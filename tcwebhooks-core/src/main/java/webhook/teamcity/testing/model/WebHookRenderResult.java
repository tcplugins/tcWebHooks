package webhook.teamcity.testing.model;

import lombok.Data;

@Data
public class WebHookRenderResult {
	
	public WebHookRenderResult(String html, String format) {
		this.html = html;
		this.format = format;
		this.errored = false;
	}
	
	public WebHookRenderResult(String text, Exception exception) {
		this.html = text;
		this.exception = exception;
		this.errored = true;
	}
	
	String html;
	String format;
	Boolean errored;
	Exception exception;

}
