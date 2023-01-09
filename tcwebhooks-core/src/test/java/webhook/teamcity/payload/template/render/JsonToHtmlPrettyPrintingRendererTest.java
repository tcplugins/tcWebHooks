package webhook.teamcity.payload.template.render;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import webhook.teamcity.payload.template.render.WebHookStringRenderer.WebHookHtmlRendererException;

public class JsonToHtmlPrettyPrintingRendererTest {

	private static final String POST_CODE_JSON = "}</code></pre>";
    private static final String PRE_CODE_JSON = "<pre><code class=\"json\">{\n";
    @SuppressWarnings("deprecation")
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testRender() throws WebHookHtmlRendererException {
		JsonToHtmlPrettyPrintingRenderer j = new JsonToHtmlPrettyPrintingRenderer();
		exception.expect(WebHookHtmlRendererException.class);
		j.render("{sdldsfj {]}");
	}

	@Test(expected = WebHookHtmlRendererException.class)
	public void testRenderWithOutput() throws WebHookHtmlRendererException {
		JsonToHtmlPrettyPrintingRenderer j = new JsonToHtmlPrettyPrintingRenderer();
		j.render("{sdldsfj {]}");
	}

	@Test
	public void testRender2WithOutput() {
		try {
			JsonToHtmlPrettyPrintingRenderer j = new JsonToHtmlPrettyPrintingRenderer();
			String out = 
				j.render("{ \"text\": \"All your build failures are belong to us\", \"attachments\": [ "
					+ "{ \"fallback\": \"${buildName} <${buildStatusUrl}|build #${buildNumber}> "
					+ "triggered by ${triggeredBy} has a status of ${buildResult}\", "
					+ "\"text\": \"${buildName} <${buildStatusUrl}|build #${buildNumber}> "
					+ "triggered by ${triggeredBy} has a status of ${buildResult}\", \"color\": \"danger\" }]}")
			;
			assertEquals( PRE_CODE_JSON
			        + "  &quot;text&quot;: &quot;All your build failures are belong to us&quot;,\n"
			        + "  &quot;attachments&quot;: [\n"
			        + "    {\n"
			        + "      &quot;fallback&quot;: &quot;${buildName} &lt;${buildStatusUrl}|build #${buildNumber}&gt; triggered by ${triggeredBy} has a status of ${buildResult}&quot;,\n"
			        + "      &quot;text&quot;: &quot;${buildName} &lt;${buildStatusUrl}|build #${buildNumber}&gt; triggered by ${triggeredBy} has a status of ${buildResult}&quot;,\n"
			        + "      &quot;color&quot;: &quot;danger&quot;\n"
			        + "    }\n"
			        + "  ]\n"
			        + POST_CODE_JSON, out);
			System.out.println(out);
		} catch (WebHookHtmlRendererException ex){
			System.out.println(ex.getMessage());
		}
	}
	
	@Test
	public void testWithFunnyChar() throws WebHookHtmlRendererException {
	    JsonToHtmlPrettyPrintingRenderer j = new JsonToHtmlPrettyPrintingRenderer();
	    String out = j.render("{ \"\u003c\": \"\"}");
	    System.out.println(out);
	    assertEquals(PRE_CODE_JSON + "  &quot;&lt;&quot;: &quot;&quot;\n" + POST_CODE_JSON, 
	            out);
	}
}
