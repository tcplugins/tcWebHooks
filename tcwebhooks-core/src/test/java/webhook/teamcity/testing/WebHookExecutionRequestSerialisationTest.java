package webhook.teamcity.testing;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookExecutionRequestGsonBuilder;

public class WebHookExecutionRequestSerialisationTest {
	
	@Test
	public void testWebHookExecutionRequestDeserialisation() throws IOException {
		
		BufferedReader reader = Files.newBufferedReader(new File("src/test/resources/testWebHookRequest/webhook-request-01.json").toPath(), StandardCharsets.UTF_8);
		WebHookExecutionRequest webHookExecutionRequest = WebHookExecutionRequestGsonBuilder.gsonBuilder().fromJson(reader, WebHookExecutionRequest.class);
		assertEquals(Long.valueOf(2834L), webHookExecutionRequest.getBuildId());
		assertEquals(true, webHookExecutionRequest.getConfigBuildStates().get(BuildStateEnum.BUILD_SUCCESSFUL));
		assertEquals(false, webHookExecutionRequest.getConfigBuildStates().get(BuildStateEnum.BUILD_STARTED));
		System.out.println(WebHookExecutionRequestGsonBuilder.gsonBuilder().toJson(webHookExecutionRequest));
	}

}
