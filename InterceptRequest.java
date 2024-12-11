import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.bidi.network.AddInterceptParameters;
import org.openqa.selenium.bidi.network.ContinueRequestParameters;
import org.openqa.selenium.bidi.network.Cookie;
import org.openqa.selenium.bidi.network.FetchTimingInfo;
import org.openqa.selenium.bidi.network.Header;
import org.openqa.selenium.bidi.network.InterceptPhase;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.IOException;
import java.lang.Thread;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class InterceptRequest {

	static protected WebDriver driver;
	static protected Network network;

	public static void main(String[] args) throws InterruptedException, IOException {

		var options = new FirefoxOptions();
		options.enableBiDi();
		driver = new FirefoxDriver(options);
		network = new Network(driver);

		network.addIntercept(new AddInterceptParameters(InterceptPhase.BEFORE_REQUEST_SENT));

		CountDownLatch latch = new CountDownLatch(2);

		network.onBeforeRequestSent(beforeRequestSent -> {
			String requestId = beforeRequestSent.getRequest().getRequestId();
			FetchTimingInfo timings = beforeRequestSent.getRequest().getTimings();
			String url = beforeRequestSent.getRequest().getUrl();
			String method = beforeRequestSent.getRequest().getMethod();
			List<Cookie> cookies = beforeRequestSent.getRequest().getCookies();
			List<Header> headers = beforeRequestSent.getRequest().getHeaders();
			Long headersSize = beforeRequestSent.getRequest().getHeadersSize();

			System.out.printf("%nRequest method %s %n. "
					+ "Sent to URL %s %n. "
					+ "Timing info %s %n. "
					+ "Cookies %s %n. "
					+ "Headers %s %n. "
					+ "Headers size %s %n.", 
					method, url, timings.getRequestTime(), cookies, headers, headersSize);

			network.continueRequest(new ContinueRequestParameters(requestId));

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
