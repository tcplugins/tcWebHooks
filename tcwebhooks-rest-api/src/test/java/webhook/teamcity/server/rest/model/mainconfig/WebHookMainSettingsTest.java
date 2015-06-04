package webhook.teamcity.server.rest.model.mainconfig;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import jetbrains.buildServer.serverSide.SBuildServer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

import webhook.WebHookProxyConfig;
import webhook.teamcity.settings.WebHookMainSettings;

public class WebHookMainSettingsTest {
	SBuildServer server = mock(SBuildServer.class);
	Integer proxyPort = 8080;
	String proxyHost = "myproxy.mycompany.com";
	
	@Test
	public void TestFullConfig(){
		WebHookMainSettings whms = new WebHookMainSettings(server);
		whms.register();
		whms.readFrom(getFullConfigElement());
		String proxy = whms.getProxyForUrl("http://something.somecompany.com");
		WebHookProxyConfig whpc = whms.getProxyConfigForUrl("http://something.somecompany.com");
		assertTrue(proxy.equals(this.proxyHost));
		assertTrue(whpc.getProxyHost().equals(this.proxyHost ));
		assertTrue(whpc.getProxyPort().equals(this.proxyPort));
	}
	
	private Element getFullConfigElement(){
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);
		try {
			Document doc = builder.build("src/test/resources/main-config-full.xml");
			return doc.getRootElement();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
