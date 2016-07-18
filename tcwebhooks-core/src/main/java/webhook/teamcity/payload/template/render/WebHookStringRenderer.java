package webhook.teamcity.payload.template.render;

import java.util.Map;

public interface WebHookStringRenderer {
	public abstract String render(String input) throws WebHookHtmlRendererException;
	public abstract String render(Map<String,String[]> input) throws WebHookHtmlRendererException;
	
	public static class WebHookHtmlRendererException extends Exception {

		private static final long serialVersionUID = 1L;
		
		public WebHookHtmlRendererException(Exception ex) {
			super(ex);
		}
		
		public WebHookHtmlRendererException(String message) {
			super(message);
		}
		
	}
}
