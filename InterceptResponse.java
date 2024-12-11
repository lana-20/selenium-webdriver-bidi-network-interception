import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.bidi.network.AddInterceptParameters;
import org.openqa.selenium.bidi.network.ContinueResponseParameters;
import org.openqa.selenium.bidi.network.FetchTimingInfo;
import org.openqa.selenium.bidi.network.InterceptPhase;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.IOException;
import java.lang.Thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class InterceptResponse {

	static protected WebDriver driver;
	static protected Network network;

	public static void main(String[] args) throws InterruptedException, IOException {

		var options = new FirefoxOptions();
		options.enableBiDi();

		driver = new FirefoxDriver(options);
		network = new Network(driver);

		network.addIntercept(new AddInterceptParameters(InterceptPhase.RESPONSE_STARTED));

		CountDownLatch latch = new CountDownLatch(2);

		network.onResponseStarted(responseDetails -> {
			String responseId = responseDetails.getRequest().getRequestId();
			FetchTimingInfo timings = responseDetails.getRequest().getTimings();
			String url = responseDetails.getRequest().getUrl();
			
			System.out.printf("%nResponse sent for URL %s %n. "
					+ "Timing info %s %n", 
					url, timings.getRequestTime());
					
			if (url.contains("selenium.dev")) {
				ContinueResponseParameters responseParams = new ContinueResponseParameters(responseId).statusCode(500);
				network.continueResponse(responseParams);
			} else {
			network.continueResponse(new ContinueResponseParameters(responseId));
			}
			
			latch.countDown();

		});

		driver.get("https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html");

		boolean countdown = latch.await(5, TimeUnit.SECONDS);

		assert (countdown);

		Thread.sleep(3000);

		network.close();
		driver.quit();
	};
}
