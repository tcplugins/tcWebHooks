package webhook.teamcity.settings.config.builder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.junit.Test;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.template.ElasticSearchWebHookTemplate;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;

public class WebHookTemplateConfigBuilderTest {
	
	SBuildServer mockServer = mock(SBuildServer.class);
	WebHookTemplateManager wtm = mock(WebHookTemplateManager.class);
	WebHookPayloadManager wpm;

	@Test
	public void testBuild() {
		
		
//		when(mockServer.getRootUrl()).thenReturn("http://test.url");
//		wpm = new WebHookPayloadManager(mockServer);
//		wtm = new WebHookTemplateManager(wpm, new WebHookTemplateJaxHelperImpl());
		ElasticSearchWebHookTemplate elasticTemplate = new ElasticSearchWebHookTemplate(wtm);
		
/*		WebHookTemplateConfig config = WebHookTemplateConfigBuilder.build(elasticTemplate);

		WebHookTemplateEntity entity = builder.build();

		assertEquals(elasticTemplate.getTemplateShortName(), entity.getName());
		assertEquals(elasticTemplate.getTemplateDescription(), entity.getTemplateDescription());
		assertEquals(elasticTemplate.getTemplateToolTip(), entity.getTemplateToolTip());*/
		
		//assertEquals(elasticTemplate.getLoggingName() entity.getDefaultTemplate().getTemplateContent()
		
		
		//fail("Not yet implemented");
	}

}
