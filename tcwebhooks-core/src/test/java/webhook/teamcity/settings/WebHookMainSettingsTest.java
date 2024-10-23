package webhook.teamcity.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.WebHookProxyConfig;

public class WebHookMainSettingsTest {
	SBuildServer server = mock(SBuildServer.class);
	Integer proxyPort = 8080;
	String proxyHost = "myproxy.mycompany.com";

	@Test
	public void TestFullConfig() throws JDOMException, IOException{
		WebHookMainSettings whms = new WebHookMainSettings(server);
		whms.register();
		whms.readFrom(getFullConfigElement());
		String proxy = whms.getProxyForUrl("http://something.somecompany.com");
		WebHookProxyConfig whpc = whms.getProxyConfigForUrl("http://something.somecompany.com");
		assertEquals(proxy, this.proxyHost);
		assertEquals(whpc.getProxyHost(), this.proxyHost);
		assertEquals(whpc.getProxyPort(), this.proxyPort);

		assertEquals(50, whms.getHttpConnectionTimeout());
		assertEquals(55, whms.getHttpResponseTimeout());
		assertEquals("Using WebHooks in myCompany Inc.", whms.getInfoText());
		assertEquals("http://intranet.mycompany.com/docs/UsingWebHooks", whms.getInfoUrl());
		assertEquals(Boolean.TRUE, whms.getWebhookShowFurtherReading());
		assertEquals(Boolean.TRUE, whms.isAssembleStatisticsEnabled());
		assertEquals(Boolean.TRUE, whms.isReportStatisticsEnabled());
		assertEquals(5, whms.getReportStatisticsFrequency());
		assertEquals(Boolean.TRUE, whms.getWebHookMainConfig().useDedicatedThreadPool());
		assertEquals(10, whms.getWebHookMainConfig().getMinPoolSize());
		assertEquals(20, whms.getWebHookMainConfig().getMaxPoolSize());
		assertEquals(1000, whms.getWebHookMainConfig().getQueueSize());
		assertEquals(Boolean.TRUE, whms.getWebHookMainConfig().isBuildStatisticsCollatorEnabled());
		assertEquals(20, whms.getWebHookMainConfig().getCheckInterval());
		assertEquals(600, whms.getWebHookMainConfig().getFailureTimeout());
		assertEquals(300, whms.getWebHookMainConfig().getBuildCompletedTimeout());
	}

	/*
		<?xml version="1.0" encoding="UTF-8"?>
		<server>
		  <webhooks useThreadedExecutor="true">
			<dedicatedThreadPool enabled="true" minPoolSize="10" maxPoolSize="20" queueSize="1000"/>
			<proxy host="myproxy.mycompany.com" port="8080" proxyShortNames="true" username="test_user" password="test_pass">
			  <noproxy url=".mycompany.com" />
			  <noproxy url="192.168.0." />
			</proxy>
			<info url="http://intranet.mycompany.com/docs/UsingWebHooks" text="Using WebHooks in myCompany Inc." show-reading="true"/>
			<statistics enabled="true">
			  <reporting enabled="true" frequency="5"/>
			</statistics>
			<http-timeout connect="50" response="55"/>
		  </webhooks>
		</server>
	 */
	@Test
	public void TestWriteOfFullConfig() throws JDOMException, IOException {
		WebHookMainSettings whms = new WebHookMainSettings(server);
		whms.register();
		whms.readFrom(getFullConfigElement());
		Element e = new Element("server");
		whms.writeTo(e);
		Element webHooksElement = e.getChild("webhooks");
		assertTrue(webHooksElement.getAttribute(WebHookMainSettings.ATTRIBUTENAME_USE_THREADED_EXECUTOR).getBooleanValue());
		Element proxyElement = webHooksElement.getChild("proxy");
		assertEquals("myproxy.mycompany.com", proxyElement.getAttribute("host").getValue());
		assertEquals(8080, proxyElement.getAttribute("port").getIntValue());
		assertEquals(true, proxyElement.getAttribute("proxyShortNames").getBooleanValue());
		assertEquals("test_user", proxyElement.getAttribute("username").getValue());
		assertEquals("test_pass", proxyElement.getAttribute("password").getValue());

		@SuppressWarnings("unchecked")
		List<Element> namedChildren = proxyElement.getChildren("noproxy");
		assertEquals(".mycompany.com", namedChildren.get(0).getAttribute("url").getValue());
		assertEquals("192.168.0.", namedChildren.get(1).getAttribute("url").getValue());

		Element infoElement = webHooksElement.getChild("info");
		assertEquals("http://intranet.mycompany.com/docs/UsingWebHooks", infoElement.getAttribute("url").getValue());
		assertEquals("Using WebHooks in myCompany Inc.", infoElement.getAttribute("text").getValue());
		assertEquals(true, infoElement.getAttribute("show-reading").getBooleanValue());

		Element statsElement = webHooksElement.getChild("statistics");
		assertEquals(true, statsElement.getAttribute("enabled").getBooleanValue());
		Element statsReportingElement = statsElement.getChild("reporting");
		assertEquals(true, statsReportingElement.getAttribute("enabled").getBooleanValue());
		assertEquals(5, statsReportingElement.getAttribute("frequency").getIntValue());

		Element httpTimeoutElement = webHooksElement.getChild("http-timeout");
		assertEquals(50, httpTimeoutElement.getAttribute("connect").getIntValue());
		assertEquals(55, httpTimeoutElement.getAttribute("response").getIntValue());

		Element threadElement = webHooksElement.getChild(WebHookMainSettings.ATTRIBUTENAME_DEDICATED_THREAD_POOL);
		assertEquals(true, threadElement.getAttribute(WebHookMainSettings.ATTRIBUTENAME_ENABLED).getBooleanValue());
		assertEquals(10, threadElement.getAttribute(WebHookMainSettings.ATTRIBUTENAME_MIN_POOL_SIZE).getIntValue());
		assertEquals(20, threadElement.getAttribute(WebHookMainSettings.ATTRIBUTENAME_MAX_POOL_SIZE).getIntValue());
		assertEquals(1000, threadElement.getAttribute(WebHookMainSettings.ATTRIBUTENAME_QUEUE_SIZE).getIntValue());

		Element buildStatsElement = webHooksElement.getChild(WebHookMainSettings.ELEMENTNAME_BUILD_STATISTICS_COLLATOR);
		assertEquals(true, buildStatsElement.getAttribute(WebHookMainSettings.ATTRIBUTENAME_ENABLED).getBooleanValue());

	}

	@Test
	public void testWriteWithEmptySettings() {
		Element e = new Element("server");
		WebHookMainSettings whms = new WebHookMainSettings(server);
		whms.writeTo(e);
		assertTrue(e.getChild("webhooks").getAttributes().isEmpty());
		assertTrue(e.getChild("webhooks").getChildren().isEmpty());
	}

	@Test
	public void testWriteWithThreadedExecutorAsFalse() throws DataConversionException {
		Element e = new Element("server");
		WebHookMainSettings whms = new WebHookMainSettings(server);
		whms.getWebHookMainConfig().setThreadPoolExecutor(false);
		whms.writeTo(e);
		assertFalse(e.getChild("webhooks").getAttribute(WebHookMainSettings.ATTRIBUTENAME_USE_THREADED_EXECUTOR).getBooleanValue());
		assertTrue(e.getChild("webhooks").getChildren().isEmpty());
	}

	@Test
	public void testWriteWithThreadedExecutorAsTrue() throws DataConversionException {
		Element e = new Element("server");
		WebHookMainSettings whms = new WebHookMainSettings(server);
		whms.getWebHookMainConfig().setThreadPoolExecutor(true);
		whms.writeTo(e);
		assertTrue(e.getChild("webhooks").getChildren().isEmpty());
		assertTrue(e.getChild("webhooks").getAttribute(WebHookMainSettings.ATTRIBUTENAME_USE_THREADED_EXECUTOR).getBooleanValue());
	}

	@Test
	public void testDedicatedThreadPoolDefaultValues() {
		WebHookMainSettings whms = new WebHookMainSettings(server);
		assertEquals(Boolean.TRUE, whms.getWebHookMainConfig().useDedicatedThreadPool());
		assertEquals(1, whms.getWebHookMainConfig().getMinPoolSize());
		assertEquals(50, whms.getWebHookMainConfig().getMaxPoolSize());
		assertEquals(3000, whms.getWebHookMainConfig().getQueueSize());
	}
	
	@Test
	public void testBuildStatisticsDefaultValues() {
	    WebHookMainSettings whms = new WebHookMainSettings(server);
	    assertEquals(Boolean.FALSE, whms.getWebHookMainConfig().isBuildStatisticsCollatorEnabled());
	    assertEquals(10, whms.getWebHookMainConfig().getCheckInterval());
	    assertEquals(3600, whms.getWebHookMainConfig().getFailureTimeout());
	}

	private Element getFullConfigElement() throws JDOMException, IOException{
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);
		Document doc = builder.build("src/test/resources/main-config-full.xml");
		return doc.getRootElement();
	}

}
