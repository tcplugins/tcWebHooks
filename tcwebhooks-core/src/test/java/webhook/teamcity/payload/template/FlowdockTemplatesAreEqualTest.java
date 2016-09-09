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

public class FlowdockTemplatesAreEqualTest {

	private WebHookContentBuilder webHookContentBuilder;
	private WebHookTemplateResolver templateResolver;
	private WebHookTemplateJaxHelper webHookTemplateJaxHelper;

	@Test
	public void TestThatTemplatesAreIdentical() {
		SBuildServer sBuildServer = mock(SBuildServer.class);
		WebHookTemplateJaxHelper webHookTemplateJaxHelper = new WebHookTemplateJaxTestHelper();
		WebHookPayloadManager payloadManager = new WebHookPayloadManager(sBuildServer);
		WebHookTemplateManager templateManager = new WebHookTemplateManager(payloadManager, webHookTemplateJaxHelper);
		FlowdockWebHookTemplate flowdockTemplate = new FlowdockWebHookTemplate(templateManager);
		flowdockTemplate.setRank(14);
		flowdockTemplate.register();
		FlowdockXmlWebHookTemplate flowdockXmlTemplate = new FlowdockXmlWebHookTemplate(templateManager, payloadManager, webHookTemplateJaxHelper);
		flowdockXmlTemplate.register();
		
		assertEquals(flowdockTemplate.getTemplateDescription().trim(), flowdockXmlTemplate.getTemplateDescription().trim());
		assertEquals(flowdockTemplate.getRank(), flowdockXmlTemplate.getRank());
		assertEquals(flowdockTemplate.getTemplateToolTip().trim(), flowdockXmlTemplate.getTemplateToolTip().trim());

		for (BuildStateEnum state : BuildStateEnum.getNotifyStates()) {
			
			WebHookTemplateContent T1 = flowdockTemplate.getTemplateForState(state);
			WebHookTemplateContent T2 = flowdockXmlTemplate.getTemplateForState(state);
			
			if (T1 == null && T2 == null){
				continue;
			}
			
			WebHookTemplateContent T1Branch = flowdockTemplate.getBranchTemplateForState(state);
			WebHookTemplateContent T2Branch = flowdockXmlTemplate.getBranchTemplateForState(state);
			
			if (T1Branch == null && T2Branch == null){
				continue;
			}
			
			assertEquals(state.getShortName() + " non-branch should match", T1.getTemplateText().trim(),
						 T2.getTemplateText().trim());
			
			assertEquals(state.getShortName() + " branch should match", T1Branch.getTemplateText().trim(),
						 T2Branch.getTemplateText().trim());
			
		}
	}

}
