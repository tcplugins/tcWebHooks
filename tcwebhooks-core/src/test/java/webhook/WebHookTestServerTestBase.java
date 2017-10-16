package webhook;

public abstract class WebHookTestServerTestBase {
	
	public abstract String getHost();
	public abstract Integer getPort();
	
	
	public WebHookTestServer startWebServer(){
		try {
			WebHookTestServer s = new WebHookTestServer(getHost(), getPort());
			s.getServer().start();
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void stopWebServer(WebHookTestServer s) throws InterruptedException {
		try {
			s.getServer().stop();
			// Sleep to let the server shutdown cleanly.
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Thread.sleep(100);
		}
	}

}
