package webhook.teamcity.executor;

import java.util.concurrent.ExecutorService;

public interface WebHookThreadingExecutorFactory {

	public ExecutorService getExecutorService();

}
