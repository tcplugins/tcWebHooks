package webhook.teamcity;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static webhook.teamcity.WebHookPluginDataResolverImpl.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.plugins.PluginManager;
import jetbrains.buildServer.plugins.bean.PluginInfo;

public class WebHookPluginDataResolverImplTest {
	
	@Mock
	PluginManager pluginManager;
	
	@Mock
	PluginInfo restPlugin;
	
	@Mock
	PluginInfo corePlugin;
	
	@Mock
	PluginInfo anotherPlugin;

	private void initMockData() {
		MockitoAnnotations.initMocks(this);
		when(restPlugin.getPluginName()).thenReturn(REST_API_PLUGIN_NAME);
		when(restPlugin.getPluginVersion()).thenReturn("1.2.3.4");
		when(corePlugin.getPluginName()).thenReturn(CORE_PLUGIN_NAME);
		when(corePlugin.getPluginVersion()).thenReturn("1.2.3.4");
		when(anotherPlugin.getPluginName()).thenReturn("anotherPlugin");
		when(anotherPlugin.getPluginVersion()).thenReturn("1.2.3.4");
		List<PluginInfo> plugins = new ArrayList<>();
		plugins.add(restPlugin);
		plugins.add(corePlugin);
		plugins.add(anotherPlugin);
		when(pluginManager.getDetectedPlugins()).thenReturn(plugins);
	}
	
	@Test
	public void testIsWebHooksRestApiInstalled() {
		initMockData();
		WebHookPluginDataResolver resolver = new WebHookPluginDataResolverImpl(pluginManager);
		assertTrue(resolver.isWebHooksRestApiInstalled());
	}


	@Test
	public void testGetWebHooksRestApiVersion() {
		initMockData();
		WebHookPluginDataResolver resolver = new WebHookPluginDataResolverImpl(pluginManager);
		assertEquals("1.2.3.4", resolver.getWebHooksRestApiVersion());
	}
	
	@Test
	public void testGetWebHooksRestApiVersionAfterIsInstalled() {
		initMockData();
		WebHookPluginDataResolver resolver = new WebHookPluginDataResolverImpl(pluginManager);
		assertTrue(resolver.isWebHooksRestApiInstalled());
		assertEquals("1.2.3.4", resolver.getWebHooksRestApiVersion());
	}
	
	@Test
	public void testIsInstalledAfterGetWebHooksRestApiVersion() {
		initMockData();
		WebHookPluginDataResolver resolver = new WebHookPluginDataResolverImpl(pluginManager);
		assertEquals("1.2.3.4", resolver.getWebHooksRestApiVersion());
		assertTrue(resolver.isWebHooksRestApiInstalled());
	}

	@Test
	public void testIsWebHooksCoreAndApiVersionTheSamePassesWhenTheyAreTheSame() {
		initMockData();
		WebHookPluginDataResolver resolver = new WebHookPluginDataResolverImpl(pluginManager);
		assertTrue(resolver.isWebHooksCoreAndApiVersionTheSame());
	}
	
	@Test
	public void testIsWebHooksCoreAndApiVersionTheSameFailsWhenTheyAreDifferent() {
		initMockData();
		when(corePlugin.getPluginVersion()).thenReturn("1.2.3.5");
		WebHookPluginDataResolver resolver = new WebHookPluginDataResolverImpl(pluginManager);
		assertFalse(resolver.isWebHooksCoreAndApiVersionTheSame());
	}

	@Test
	public void testGetWebHooksCoreVersion() {
		initMockData();
		WebHookPluginDataResolver resolver = new WebHookPluginDataResolverImpl(pluginManager);
		assertEquals("1.2.3.4", resolver.getWebHooksCoreVersion());
	}
	
	@Test
	public void testGetWebHooksCoreVersionAfterIsWebHooksCoreAndApiVersionTheSame() {
		initMockData();
		WebHookPluginDataResolver resolver = new WebHookPluginDataResolverImpl(pluginManager);
		when(corePlugin.getPluginVersion()).thenReturn("1.2.3.5");
		assertFalse(resolver.isWebHooksCoreAndApiVersionTheSame());
		assertEquals("1.2.3.5", resolver.getWebHooksCoreVersion());
	}

}
