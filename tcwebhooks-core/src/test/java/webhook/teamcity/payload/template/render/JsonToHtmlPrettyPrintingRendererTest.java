package webhook.teamcity.payload.template.render;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import webhook.teamcity.payload.template.render.WebHookStringRenderer.WebHookHtmlRendererException;

public class JsonToHtmlPrettyPrintingRendererTest {
	
	  @Rule
	  public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testRender() throws WebHookHtmlRendererException {
		JsonToHtmlPrettyPrintingRenderer j = new JsonToHtmlPrettyPrintingRenderer();
		exception.expect(WebHookHtmlRendererException.class);
		j.render("{sdldsfj {]}");
	}

	@Test
	public void testRenderWithOutput() {
		try {
			JsonToHtmlPrettyPrintingRenderer j = new JsonToHtmlPrettyPrintingRenderer();
			j.render("{sdldsfj {]}");
		} catch (WebHookHtmlRendererException ex){
			System.out.println(ex.getMessage());
		}
	}
}
