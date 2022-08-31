package webhook.teamcity.json;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

public class WebHookConfigurationGsonBuilderTest {

	@Test
	public void test() throws IOException {
		
		BufferedReader reader = Files.newBufferedReader(new File("src/test/resources/webhook/webhook-configuration.json").toPath(), StandardCharsets.UTF_8);
		WebHookConfigurationJson webhookConfig = WebHookConfigurationGsonBuilder.gsonBuilder().fromJson(reader, WebHookConfigurationJson.class);
		
		assertEquals(true, webhookConfig.getEnabled());
		assertEquals("http://my.somewhere.example/webhook", webhookConfig.getUrl());
		
		assertEquals(1, webhookConfig.getHeaders().getHeader().size());
		assertEquals(1, webhookConfig.getFilters().getFilter().size());
		assertEquals(1, webhookConfig.getParameters().getParameter().size());
		
	}

}
