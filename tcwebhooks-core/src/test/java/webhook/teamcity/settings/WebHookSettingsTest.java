package webhook.teamcity.settings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

import webhook.WebHook;
import webhook.WebHookImpl;
import webhook.WebHookProxyConfig;
import webhook.WebHookTest;
import webhook.WebHookTestProxyServer;
import webhook.WebHookTestServer;


public class WebHookSettingsTest {
	
	@Test
	public void test_SingleProxyHostRegex(){
		WebHookMainConfig mainConfig = new WebHookMainConfig();
		mainConfig.setProxyShortNames(false);
		assertFalse(mainConfig.matchProxyForURL("test"));
		assertNull(mainConfig.getProxyConfigForUrl("test"));
		mainConfig.setProxyHost("someproxy");
		mainConfig.setProxyPort(8080);
		assertFalse(mainConfig.matchProxyForURL("test"));
		assertNull(mainConfig.getProxyConfigForUrl("test"));

		mainConfig.setProxyShortNames(true);
		WebHookProxyConfig proxyConfig = mainConfig.getProxyConfigForUrl("test");
		assertTrue(proxyConfig.getProxyHost().equals("someproxy"));
		assertTrue(proxyConfig.getProxyPort().equals(8080));

		mainConfig.setProxyShortNames(false);
		assertNull(mainConfig.getProxyConfigForUrl("test"));
		
		
	}

	@Test 
	public void test_Regex(){
		WebHookMainConfig mainConfig = new WebHookMainConfig();
		assertTrue(mainConfig.isUrlShortName("test"));
		assertTrue(mainConfig.isUrlShortName("test:80"));
		
		assertTrue(mainConfig.isUrlShortName("http://test"));
		assertTrue(mainConfig.isUrlShortName("http://test:80"));
		assertTrue(mainConfig.isUrlShortName("http://test/"));
		assertTrue(mainConfig.isUrlShortName("http://test:80/"));
		assertTrue(mainConfig.isUrlShortName("http://test/testtest"));
		assertTrue(mainConfig.isUrlShortName("http://test:80/testtest"));
		
		assertTrue(mainConfig.isUrlShortName("https://test"));
		assertTrue(mainConfig.isUrlShortName("https://test:80"));
		assertTrue(mainConfig.isUrlShortName("https://test/"));
		assertTrue(mainConfig.isUrlShortName("https://test:80/"));
		assertTrue(mainConfig.isUrlShortName("https://test/testtest"));
		assertTrue(mainConfig.isUrlShortName("https://test:80/testtest"));
		
		assertFalse(mainConfig.isUrlShortName("test.test"));
		assertFalse(mainConfig.isUrlShortName("test.test:80"));		
		assertFalse(mainConfig.isUrlShortName("http://test.test"));
		assertFalse(mainConfig.isUrlShortName("http://test.test:80"));
		assertFalse(mainConfig.isUrlShortName("http://test.test/"));
		assertFalse(mainConfig.isUrlShortName("http://test.test:80/"));
		assertFalse(mainConfig.isUrlShortName("http://test.test/testteset"));
		assertFalse(mainConfig.isUrlShortName("http://test.test:80/testteset"));
		
		System.out.println(mainConfig.getHostNameFromUrl("http://test.test.test/"));
		
	}
	
	@Test
	public void test_ProxyUrlMatching(){
		WebHookMainConfig mainConfig = new WebHookMainConfig();
		mainConfig.addNoProxyUrl(".example.com");
		mainConfig.addNoProxyUrl("192.168.0.");
		mainConfig.addNoProxyUrl("10.");
		mainConfig.setProxyShortNames(false);
		mainConfig.setProxyHost("localhost");
		mainConfig.setProxyPort(8002);
		assertFalse(mainConfig.matchProxyForURL("test"));
		
		assertTrue(mainConfig.matchProxyForURL("http://test.test.test/test?state=test"));
		assertTrue(mainConfig.matchProxyForURL("http://test.test.test/test?state=test"));
		assertFalse(mainConfig.matchProxyForURL("test.example.com"));
		assertFalse(mainConfig.matchProxyForURL("test1.test2.example.com/test"));
		assertFalse(mainConfig.matchProxyForURL("http://test1.test2.example.com/test"));
		assertFalse(mainConfig.matchProxyForURL("https://test1.test2.example.com/test"));
		assertTrue(mainConfig.matchProxyForURL("https://test1.test2/test/TEST.example.com/dkjf"));
		
		assertFalse(mainConfig.matchProxyForURL("192.168.0.99"));
		assertFalse(mainConfig.matchProxyForURL("192.168.0.100"));
		assertFalse(mainConfig.matchProxyForURL("http://10.10.0.1"));
		assertTrue(mainConfig.matchProxyForURL("http://100.10.10.1/test1.test2.example.com/test"));
	}
	
	@Test
	public void test_200UsingProxyFromConfig() throws FileNotFoundException, IOException, InterruptedException {
		WebHookTest test = new WebHookTest();
		WebHookMainConfig mainConfig = new WebHookMainConfig();
		mainConfig.setProxyHost(test.proxy);
		mainConfig.setProxyPort(test.proxyPort);
		mainConfig.setProxyShortNames(true);
		String url = "http://localhost:" + test.webserverPort + "/200";
		WebHook w = new WebHookImpl(url, mainConfig.getProxyConfigForUrl(url));
		WebHookTestServer s = test.startWebServer();
		WebHookTestProxyServer p = test.startProxyServer();
		w.setEnabled(true);
		w.post();
		test.stopWebServer(s);
		test.stopProxyServer(p);
		assertTrue(w.getStatus() == HttpStatus.SC_OK);		
	}

	@Test
	public void test_AuthFailWrongCredsUsingProxyFromConfig() throws FileNotFoundException, IOException, InterruptedException {
		WebHookTest test = new WebHookTest();
		WebHookMainConfig mainConfig = new WebHookMainConfig();
		mainConfig.setProxyHost(test.proxy);
		mainConfig.setProxyPort(test.proxyPort);
		mainConfig.setProxyShortNames(true);
		String url = "http://" + test.webserverHost + ":" + test.webserverPort + "/200";
		WebHook w = new WebHookImpl(url, mainConfig.getProxyConfigForUrl(url));
		w.setProxyUserAndPass("somethingIncorrect", "somethingIncorrect");
		WebHookTestServer s = test.startWebServer();
		WebHookTestProxyServer p = test.startProxyServerAuth("somthingCorrect", "somethingCorrect");
		w.setEnabled(true);
		w.post();
		test.stopWebServer(s);
		test.stopProxyServer(p);
		assertTrue(w.getStatus() == HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
	}

	@Test
	public void test_AuthFailNoCredsUsingProxyFromConfig() throws FileNotFoundException, IOException, InterruptedException {
		WebHookTest test = new WebHookTest();
		WebHookMainConfig mainConfig = new WebHookMainConfig();
		mainConfig.setProxyHost(test.proxy);
		mainConfig.setProxyPort(test.proxyPort);
		mainConfig.setProxyShortNames(true);
		String url = "http://" + test.webserverHost + ":" + test.webserverPort + "/200";
		WebHook w = new WebHookImpl(url, mainConfig.getProxyConfigForUrl(url));
		w.setProxyUserAndPass("somethingIncorrect", "somethingIncorrect");
		WebHookTestServer s = test.startWebServer();
		WebHookTestProxyServer p = test.startProxyServerAuth("somethingCorrect", "somethingCorrect");
		w.setEnabled(true);
		w.post();
		test.stopWebServer(s);
		test.stopProxyServer(p);
		assertTrue(w.getStatus() == HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
	}

	@Test
	public void test_AuthPassNoCredsUsingProxyFromConfig() throws FileNotFoundException, IOException, InterruptedException {
		WebHookTest test = new WebHookTest();
		WebHookMainConfig mainConfig = new WebHookMainConfig();
		mainConfig.setProxyHost(test.proxy);
		mainConfig.setProxyPort(test.proxyPort);
		mainConfig.setProxyShortNames(true);
		String url = "http://" + test.webserverHost + ":" + test.webserverPort + "/200";
		WebHook w = new WebHookImpl(url, mainConfig.getProxyConfigForUrl(url));
		WebHookTestServer s = test.startWebServer();
		WebHookTestProxyServer p = test.startProxyServer();
		w.setEnabled(true);
		w.post();
		test.stopWebServer(s);
		test.stopProxyServer(p);
		assertTrue(w.getStatus() == HttpStatus.SC_OK);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_WebookConfig() throws JDOMException, IOException{
		SAXBuilder builder = new SAXBuilder();
		List<WebHookConfig> configs = new ArrayList<>();
		builder.setIgnoringElementContentWhitespace(true);
			Document doc = builder.build("src/test/resources/testdoc2.xml");
			Element root = doc.getRootElement();
			if(root.getChild("webhooks") != null){
				Element child = root.getChild("webhooks");
				if ((child.getAttribute("enabled") != null) && (child.getAttribute("enabled").equals("true"))){
					List<Element> namedChildren = child.getChildren("webhook");
					for(Iterator<Element> i = namedChildren.iterator(); i.hasNext();)
		            {
						Element e = i.next();
						WebHookConfig whConfig = new WebHookConfig(e);
						configs.add(whConfig);
		            }
				}
			}

		
		for (WebHookConfig c : configs){
			WebHook wh = new WebHookImpl(c.getUrl());
			wh.setEnabled(c.getEnabled());
			//wh.addParams(c.getParams());
			System.out.println(wh.getUrl());
			System.out.println(wh.isEnabled().toString());

		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_ReadXml() throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		//builder.setValidation(true);
		builder.setIgnoringElementContentWhitespace(true);
		
			Document doc = builder.build("src/test/resources/testdoc1.xml");
			Element root = doc.getRootElement();
			System.out.println(root.toString());
			if(root.getChild("webhooks") != null){
				Element child = root.getChild("webhooks");
				if ((child.getAttribute("enabled") != null) && (child.getAttribute("enabled").equals("true"))){
					List<Element> namedChildren = child.getChildren("webhook");
					for(Iterator<Element> i = namedChildren.iterator(); i.hasNext();)
		            {
						Element e = i.next();
						System.out.println(e.toString() + e.getAttributeValue("url"));
						//assertTrue(e.getAttributeValue("url").equals("http://something"));
						if(e.getChild("parameters") != null){
							Element eParams = e.getChild("parameters");
							List<Element> paramsList = eParams.getChildren("param");
							for(Iterator<Element> j = paramsList.iterator(); j.hasNext();)
							{
								Element eParam = j.next();
								System.out.println(eParam.toString() + eParam.getAttributeValue("name"));
								System.out.println(eParam.toString() + eParam.getAttributeValue("value"));
							}
						}
		            }
				}
			}

	}
}
