package webhook.teamcity.server.rest.model.mainconfig;

import static org.junit.Assert.*;

import org.junit.Test;

public class WebhooksTest {

	@Test
	public void test() {
		Webhooks webhooks = new Webhooks();
		Information info = new Information();
		info.setUrl("http://example.com"); 
		info.setText("Some blurb"); 
		
	}

}
