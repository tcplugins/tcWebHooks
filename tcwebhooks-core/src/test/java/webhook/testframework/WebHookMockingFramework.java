package webhook.testframework;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;

import org.jdom.JDOMException;

import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;

public interface WebHookMockingFramework {
	
	public SBuildServer getServer();
	public SRunningBuild getRunningBuild();
	public SBuildType getSBuildType();
	public SBuildType getSBuildTypeFromSubProject();
	public WebHookConfig getWebHookConfig();
	public WebHookPayloadContent getWebHookContent();
	public WebHookPayloadManager getWebHookPayloadManager();
	public WebHookProjectSettings getWebHookProjectSettings();
	public WebHookSettingsManager getWebHookSettingsManager();
	public WebHookTemplateManager getWebHookTemplateManager();
	public WebHookTemplateResolver getWebHookTemplateResolver();
	public WebHookAuthenticatorProvider getWebHookAuthenticatorProvider();
	public WebHookVariableResolverManager getWebHookVariableResolverManager();
	public WebHookListener getWebHookListener();
	public void loadWebHookConfigXml(File xmlConfigFile) throws JDOMException, IOException;
	public void loadNthWebHookConfigXml(int itemNumber, File xmlConfigFile) throws JDOMException, IOException;
	public void loadWebHookProjectSettingsFromConfigXml(File xmlConfigFile) throws IOException, JDOMException;
	public List<SFinishedBuild> getMockedBuildHistory();
	public SFinishedBuild getPreviousFailedBuild();
	public SFinishedBuild getPreviousSuccessfulBuild();
	public ProjectIdResolver getProjectIdResolver();
}
