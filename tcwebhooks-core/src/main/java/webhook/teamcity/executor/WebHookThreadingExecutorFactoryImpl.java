package webhook.teamcity.executor;

import java.util.concurrent.ExecutorService;

import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.executors.ExecutorsFactory;
import lombok.AllArgsConstructor;
import webhook.teamcity.settings.WebHookMainSettings;

@AllArgsConstructor
public class WebHookThreadingExecutorFactoryImpl implements WebHookThreadingExecutorFactory {
    
    private final ExecutorServices executorServices;
    private final WebHookMainSettings myMainSettings;

    private ExecutorService scheduledExecutorService;
    @Override
    public ExecutorService getExecutorService() {
        if (!this.myMainSettings.getWebHookMainConfig().useThreadedExecutor()) {
            return executorServices.getNormalExecutorService();
        }
        
        if (this.scheduledExecutorService == null) {
           this.scheduledExecutorService = ExecutorsFactory.newFixedDaemonExecutor(
                    "WebHookThread-", 
                    this.myMainSettings.getWebHookMainConfig().getMinPoolSize(), 
                    this.myMainSettings.getWebHookMainConfig().getMaxPoolSize(),
                    this.myMainSettings.getWebHookMainConfig().getQueueSize());
        }
        return this.scheduledExecutorService;
    }
    
}
