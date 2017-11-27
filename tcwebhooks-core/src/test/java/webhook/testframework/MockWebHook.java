package webhook.testframework;

import java.io.IOException;

import webhook.WebHook;
import webhook.WebHookImpl;
import webhook.WebHookProxyConfig;
import webhook.teamcity.Loggers;
import webhook.teamcity.settings.WebHookConfig;

public class MockWebHook  extends WebHookImpl implements WebHook, Mocked {
	
	int invocationCount = 0;

	public MockWebHook() {
		super();
	}
	
	public MockWebHook(WebHookConfig webHookConfig, WebHookProxyConfig pc) {
		super();
		this.setUrl(webHookConfig.getUrl());
		this.setEnabled(webHookConfig.getEnabled());
		this.setBuildStates(webHookConfig.getBuildStates());
	}

	@Override
	public void post() throws IOException {
		invocationCount++;
		this.getExecutionStats().setUrl(this.getUrl());
    	this.getExecutionStats().setRequestStarting();
    	this.getExecutionStats().setRequestCompleted(-1);
        this.getExecutionStats().setTeardownCompleted();
	}

	@Override
	public Integer getStatus() {
		return -1;
	}


	@Override
	public Boolean isErrored() {
		return true;
	}

	@Override
	public String getErrorReason() {
		return "I'm only a mock";
	}

	@Override
	public int getInvocationCount() {
		return invocationCount;
	}

}
