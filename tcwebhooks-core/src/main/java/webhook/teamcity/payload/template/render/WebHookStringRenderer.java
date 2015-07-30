package webhook.teamcity.payload.template.render;

public interface WebHookStringRenderer {
	public abstract String render(String input) throws WebHookHtmlRendererException;
	
	public static class WebHookHtmlRendererException extends Exception {

		private static final long serialVersionUID = 1L;
		
		public WebHookHtmlRendererException(Exception ex) {
			super(ex);
		}
		
	}
}
