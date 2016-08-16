package webhook.teamcity.server.rest.request;

import org.springframework.context.annotation.Bean;
import static org.mockito.Mockito.mock;
import jetbrains.buildServer.serverSide.SBuildServer;

public class RequestTestConfiguration {
	
	@Bean
	SBuildServer sBuildServer() {
		return mock(SBuildServer.class);
	}

	@Bean
	TemplateRequest templateRequest(){
		return new TemplateRequest();
	}

}
