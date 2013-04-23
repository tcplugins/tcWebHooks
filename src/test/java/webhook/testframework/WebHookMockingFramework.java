package webhook.testframework;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.settings.WebHookConfig;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;

public interface WebHookMockingFramework {
	
	public SBuildServer getServer();
	public SRunningBuild getRunningBuild();
	public WebHookConfig getWebHookConfig();
	public WebHookPayloadContent getWebHookContent();
	public void loadWebHookConfigXml(File xmlConfigFile) throws JDOMException, IOException;

}
