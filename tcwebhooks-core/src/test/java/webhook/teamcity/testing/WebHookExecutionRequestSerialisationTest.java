package webhook.teamcity.testing;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

import com.google.gson.GsonBuilder;

import webhook.teamcity.testing.model.WebHookExecutionRequest;

public class WebHookExecutionRequestSerialisationTest {
	
	@Test
	public void testWebHookExecutionRequestDeserialisation() throws IOException {
		
		BufferedReader reader = Files.newBufferedReader(new File("src/test/resources/testWebHookRequest/webhook-request-01.json").toPath(), StandardCharsets.UTF_8);
		WebHookExecutionRequest webHookExecutionRequest = new GsonBuilder().create().fromJson(reader, WebHookExecutionRequest.class);
		assertEquals(Long.valueOf(2834L), webHookExecutionRequest.getBuildId());
		
	}

}
