package webhook.teamcity.payload.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;

import org.jdom.JDOMException;
import org.junit.Test;

import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.TestingWebHookHttpClientFactoryImpl;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.WebHookFactory;
import webhook.teamcity.WebHookFactoryImpl;
import webhook.teamcity.WebHookHttpClientFactoryImpl;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.content.WebHookPayloadContentAssemblyException;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplateJaxTestHelper;
import webhook.testframework.util.ConfigLoaderUtil;

public class SlackComTemplatesAreEqualTest {

	private WebHookContentBuilder webHookContentBuilder;
	private WebHookTemplateResolver templateResolver;
	private WebHookTemplateJaxHelper webHookTemplateJaxHelper;

	@Test
	public void TestThatTemplatesAreIdentical() {
		SBuildServer sBuildServer = mock(SBuildServer.class);
		WebHookTemplateJaxHelper webHookTemplateJaxHelper = new WebHookTemplateJaxTestHelper();
		WebHookPayloadManager payloadManager = new WebHookPayloadManager(sBuildServer);
		WebHookTemplateManager templateManager = new WebHookTemplateManager(payloadManager, webHookTemplateJaxHelper);
		SlackComWebHookTemplate slackTemplate = new SlackComWebHookTemplate(templateManager);
		slackTemplate.setRank(20);
		slackTemplate.register();
		SlackComXmlWebHookTemplate slackXmlTemplate = new SlackComXmlWebHookTemplate(templateManager, payloadManager, webHookTemplateJaxHelper);
		slackXmlTemplate.register();

		assertEquals(slackTemplate.getTemplateDescription().trim(), slackXmlTemplate.getTemplateDescription().trim());
		assertEquals(slackTemplate.getRank(), slackXmlTemplate.getRank());
		assertEquals(slackTemplate.getTemplateToolTip().trim(), slackXmlTemplate.getTemplateToolTip().trim());
		
		for (BuildStateEnum state : BuildStateEnum.getNotifyStates()) {
			boolean t1Error = false;
			boolean t2Error = false;
			boolean t1BranchError = false;
			boolean t2BranchError = false;

			WebHookTemplateContent T1 = null;
			WebHookTemplateContent T2 = null;
			
			WebHookTemplateContent T1Branch = null;
			WebHookTemplateContent T2Branch = null;
			
			try {
				T1 = slackTemplate.getTemplateForState(state);
			} catch (UnSupportedBuildStateException e) {
				t1Error = true;
			}
			
			try {
				T2 = slackXmlTemplate.getTemplateForState(state);
			} catch (UnSupportedBuildStateException e) {
				t2Error = true;
			}
			if (t1Error == t2Error){
				continue;
			}
			
			try {
				T1Branch = slackTemplate.getBranchTemplateForState(state);
			} catch (UnSupportedBuildStateException e) {
				t1BranchError = true;
			}				

			try {
				T2Branch = slackXmlTemplate.getBranchTemplateForState(state);
			} catch (UnSupportedBuildStateException e) {
				t1BranchError = true;
			}
			if (t1BranchError == t2BranchError){
				continue;
			}
			
			assertEquals(state.getShortName() + " non-branch should match", T1.getTemplateText().trim(),
						 T2.getTemplateText().trim());
			
			assertEquals(state.getShortName() + " branch should match", T1Branch.getTemplateText().trim(),
						 T2Branch.getTemplateText().trim());
			
		}
	}

}
