package webhook.teamcity.buildrunner;

import java.util.HashMap;
import java.util.Map;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

public class WebHookServiceMessageRunType extends RunType {
	
	private PluginDescriptor myPluginDescriptor;

	public WebHookServiceMessageRunType(
			final RunTypeRegistry runTypeRegistry,
			final PluginDescriptor pluginDescriptor
			) 
	{
		myPluginDescriptor = pluginDescriptor;
		runTypeRegistry.registerRunType(this);
	}

	@Override
	public String getType() {
		return "tcWebHooks";
	}

	@Override
	public String getDisplayName() {
		return "WebHook Service Message Sender";
	}

	@Override
	public String getDescription() {
		return "Trigger Webhooks with 'Service Message Received' enabled";
	}

	@Override
	public PropertiesProcessor getRunnerPropertiesProcessor() {
		return new WebHookServiceMessagePropertiesProcessor();
	}

	@Override
	public String getEditRunnerParamsJspFilePath() {
		return myPluginDescriptor.getPluginResourcesPath("WebHook/viewWebHookRunnerParams.jsp");
	}

	@Override
	public String getViewRunnerParamsJspFilePath() {
		return myPluginDescriptor.getPluginResourcesPath("WebHook/editWebHookRunnerParams.jsp");
	}

	@Override
	public Map<String, String> getDefaultRunnerProperties() {
		return new HashMap<>();
	}

}
