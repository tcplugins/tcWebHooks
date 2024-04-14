package webhook.teamcity.settings;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDateTime;

import com.google.common.primitives.Ints;

import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import lombok.Data;
import webhook.teamcity.DeferrableService;
import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.Loggers;
import webhook.teamcity.WebHookSettingsEventHandler;
import webhook.teamcity.WebHookSettingsEventType;

public class WebHookProjectSettingsReloadScheduler implements DeferrableService, WebHookSettingsEventHandler {
	
	LocalDateTime lastRun = null;
	private final ScheduledExecutorService myExecutorService;
	private final DeferrableServiceManager myDeferrableServiceManager;
	private final WebHookSettingsManager myWebHookSettingsManager;
	private DelayQueue<DelayEvent> queue = new DelayQueue<>();

	private boolean shuttingDown = false;

	
	public WebHookProjectSettingsReloadScheduler(ExecutorServices executorServices, DeferrableServiceManager deferrableServiceManager, WebHookSettingsManager webHookSettingsManager) {
		this.myExecutorService = executorServices.getNormalExecutorService();
		this.myDeferrableServiceManager = deferrableServiceManager;
		this.myWebHookSettingsManager = webHookSettingsManager;
	}

	@Override
	public void requestDeferredRegistration() {
		Loggers.SERVER.info("WebHookProjectSettingsReloadScheduler :: Registering as a deferrable service");
		myDeferrableServiceManager.registerService(this);
	}

	@Override
	public void register() {
		Loggers.SERVER.info("WebHookProjectSettingsReloadScheduler :: Requesting scheduling of WebHookProjectSettingsReloadTask");
		this.myExecutorService.schedule(new WebHookProjectSettingsReloadTask(this, myWebHookSettingsManager), 10, TimeUnit.SECONDS);
	}
	
	@Override
	public void unregister() {
		this.shuttingDown = true;
	}
	
	
	public class WebHookProjectSettingsReloadTask implements Runnable {
		
		private WebHookProjectSettingsReloadScheduler myWebHookProjectSettingsReloadScheduler;
		private WebHookSettingsManager myWebHookSettingsManager;

		public WebHookProjectSettingsReloadTask(WebHookProjectSettingsReloadScheduler webHookProjectSettingsReloadScheduler, WebHookSettingsManager webHookSettingsManager) {
			this.myWebHookProjectSettingsReloadScheduler = webHookProjectSettingsReloadScheduler;
			this.myWebHookSettingsManager = webHookSettingsManager;
		}

		@Override
		public void run() {
			Loggers.SERVER.debug("WebHookProjectSettingsReloadTask :: Starting task");
			while(! this.myWebHookProjectSettingsReloadScheduler.shuttingDown) {
	            try {
	                DelayEvent object = queue.take();
	                Loggers.SERVER.debug("WebHookProjectSettingsReloadTask :: Handling deferred WebHookSettings reload event: " + object);
	                this.myWebHookSettingsManager.handleProjectChangedEvent(object.data);
	            } catch (InterruptedException e) {
	                Loggers.SERVER.warn("WebHookProjectSettingsReloadTask interrupted. Deferred reloading of WebHookSettings will no longer be undertaken. This should only happen when TeamCity is shutting down. If not, please report as a bug in tcWebHooks");
	                Thread.currentThread().interrupt();  // set interrupt flag
	                this.myWebHookProjectSettingsReloadScheduler.shuttingDown = true;
	            } catch (Exception e) {
	                Loggers.SERVER.warn("WebHookProjectSettingsReloadTask exception occured.", e);
	            }
			}
		}
	}


    @Override
    public void handleEvent(WebHookSettingsEventType eventType, String projectInternalId) {
        this.queue.add(new DelayEvent(projectInternalId, 10000));
    }
    
    @Data
    public static class DelayEvent implements Delayed {
        private String data;
        private long startTime;

        public DelayEvent(String data, long delayInMilliseconds) {
            this.data = data;
            this.startTime = System.currentTimeMillis() + delayInMilliseconds;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = startTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Ints.saturatedCast(
              this.startTime - ((DelayEvent) o).startTime);
        }

    }
}
