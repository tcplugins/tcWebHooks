package webhook.teamcity.statistics;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import webhook.teamcity.history.GeneralisedWebAddress;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.history.WebAddressTransformerImpl;

@RunWith(Parameterized.class)
public class WebAddressTransformerImplTest {
	
	private String generalisedResult;
	private String url;
	private WebAddressTransformer transformer = new WebAddressTransformerImpl();

	public WebAddressTransformerImplTest(String generalisedResult, String url) {
		super();
		this.generalisedResult = generalisedResult;
		this.url = url;
	}
	
	@Parameters(name= "{index}: generalisedUrl[{0}]")
	public static Collection<String[]> input() {
		return Arrays.asList(new String[][] {
			{"localhost", "http://localhost:1234/somewhere?over=the&rainbow=weighApie"},
			{"127.0.0.", "http://127.0.0.1:1234/somewhere?over=the&rainbow=weighApie"},
			{"10.10.10.", "http://10.10.10.1:1234/"},
			{"localdomain", "http://localhost.localdomain:1234/"},
			{"fred", "http://fred:1234/"},
			{"local.lan", "http://docker.local.lan:9200/"},
			{"local", "http://docker.local:9200/"},
			{"slack.com", "https://hooks.slack.com/api/12345678901234567890"},
			{"fe80:0:0:0:5054:ff:fe1c:511f", "https://[fe80::5054:ff:fe1c:511f]:8111/api/12345678901234567890"}
		});
	}

	@Test
	public void testGetGeneralisedHostNames() throws MalformedURLException {
		
		GeneralisedWebAddress generalisedWebAddress = transformer.getGeneralisedHostName(new URL(url));

        System.out.println(String.format("Expecting %s from URL %s", generalisedResult, url));
		assertEquals(generalisedResult, generalisedWebAddress.getGeneralisedAddress());
	}

}
