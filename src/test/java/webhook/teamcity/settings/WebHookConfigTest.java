package webhook.teamcity.settings;

import java.io.IOException;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import webhook.WebHookProxyConfig;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WebHookConfigTest {
	SBuildServer server = mock(SBuildServer.class);
	Loggers Loggers = mock(Loggers.class);
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
