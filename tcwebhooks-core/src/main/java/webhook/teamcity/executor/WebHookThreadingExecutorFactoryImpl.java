package webhook.teamcity.executor;

import java.util.concurrent.ExecutorService;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.executors.ExecutorsFactory;
import lombok.RequiredArgsConstructor;
import webhook.teamcity.DeferrableService;
import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.settings.WebHookMainSettings;

@RequiredArgsConstructor
public class WebHookThreadingExecutorFactoryImpl implements WebHookThreadingExecutorFactory, DeferrableService {
	private static final Logger LOG = Logger.getInstance(WebHookThreadingExecutorFactoryImpl.class.getName());

	private final ExecutorServices executorServices;
	private final WebHookMainSettings myMainSettings;
	private final DeferrableServiceManager deferrableServiceManager;

	private ExecutorService executorService = null;

	@Override
	public ExecutorService getExecutorService() {
		if (!this.myMainSettings.getWebHookMainConfig().useDedicatedThreadPool()) {
			return executorServices.getNormalExecutorService();
		}

		return getInstanceUsingDoubleLocking();
	}

	private ExecutorService getInstanceUsingDoubleLocking() {
		ExecutorService localResource = this.executorService;
		if (localResource == null) {
			synchronized (this) {
				localResource = this.executorService;
				if (localResource == null) {
					this.executorService = localResource = ExecutorsFactory.newFixedDaemonExecutor(
							"WebHookThread-",
							this.myMainSettings.getWebHookMainConfig().getMinPoolSize(),
							this.myMainSettings.getWebHookMainConfig().getMaxPoolSize(),
							this.myMainSettings.getWebHookMainConfig().getQueueSize());
					LOG.info(String.format(
							"WebHookThreadingExecutorFactoryImpl :: Creating WebHook Dedicated ExecutorService minPoolSize=%s, maxPoolSize=%s, queueSize=%s",
							this.myMainSettings.getWebHookMainConfig().getMinPoolSize(),
							this.myMainSettings.getWebHookMainConfig().getMaxPoolSize(),
							this.myMainSettings.getWebHookMainConfig().getQueueSize()));
				}
			}
		}
		return localResource;
	}

	@Override
	public void requestDeferredRegistration() {
		LOG.info("WebHookThreadingExecutorFactoryImpl :: Registering as a deferrable service");
		deferrableServiceManager.registerService(this);
	}

	@Override
	public void register() {
		LOG.info("WebHookThreadingExecutorFactoryImpl :: Initialising WebHook ExecutorService");
		getInstanceUsingDoubleLocking();
	}

	@Override
	public void unregister() {
		if (this.executorService != null) {
			LOG.info("WebHookThreadingExecutorFactoryImpl :: Shutting down WebHook ExecutorService");
			this.executorService.shutdownNow();
		}
	}

}
