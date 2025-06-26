package webhook.teamcity.settings;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.primitives.Ints;

import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import lombok.Data;
import webhook.teamcity.DeferrableService;
import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.Loggers;
import webhook.teamcity.WebHookSettingsEventHandler;
import webhook.teamcity.WebHookSettingsEventType;

/**
 * {@link WebHookSettingsEventHandler} implementation where events are stored in a {@link DelayQueue}
 * and then issued to the {@link WebHookProjectSettingsReloadTask} after 10 seconds.
 * <p>
 * The DelayQueue supports de-duplication of events by checking if an "equal" event
 * is already in the queue when {@link DelayQueue#add(Delayed)} is called.
 * <p>
 * TeamCity sometimes generates the same events a few milliseconds apart. We can use
 * this to build a window of time where events for the same project and with a timestamp
 * of plus or minus 1000ms from ours can be treated as "the same".
 * <p>
 * The DelayQueue won't allow events to be emitted from the queue until they timeout, 
 * and we have set the timeout at 10 seconds. So a flood of events over a 2 second range will 
 * be de-duped to 1, and then after 10 seconds our thread will handle it as a single event. 
 */
public class WebHookProjectSettingsReloadScheduler implements DeferrableService, WebHookSettingsEventHandler {
	
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
		this.myWebHookSettingsManager.initialise(); // Make sure it's initialised.
		this.myExecutorService.schedule(new WebHookProjectSettingsReloadTask(this, myWebHookSettingsManager), 10, TimeUnit.SECONDS);
	}
	
	@Override
	public void unregister() {
		this.shuttingDown = true;
	}
	
	@Override
	public void handleEvent(WebHookSettingsEvent event) {
	    if (WebHookSettingsEventType.PROJECT_CHANGED.equals(event.getEventType())) {
	        this.queue.add(new DelayEvent(event, 10000, 1000));
	    } else if (WebHookSettingsEventType.PROJECT_PERSISTED.equals(event.getEventType())) {
	            this.queue.add(new DelayEvent(event, 1000, 1000));
	    } else if (WebHookSettingsEventType.BUILD_TYPE_DELETED.equals(event.getEventType())) {
	        this.queue.add(new DelayEvent(event, 1000, 1000));
	    }
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
	                Loggers.SERVER.debug("WebHookProjectSettingsReloadTask :: Handling deferred WebHookSettings reload event: " + object.getTypeAndEvent());
	                this.myWebHookSettingsManager.handleProjectChangedEvent(object);
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


    @Data
    public static class DelayEvent implements Delayed, WebHookSettingsEvent {
        private WebHookSettingsEvent event;
        private String typeAndEvent;
        private long startTime;
        private long startTimeWindowBegin;
        private long startTimeWindowEnd;

        public DelayEvent(WebHookSettingsEvent event, long delayInMilliseconds, int windowTimeMs) {
            this.event = event;
            this.typeAndEvent = String.format("%s:%s:%s", event.getEventType().toString(), event.getProjectInternalId(), event.getBuildTypeInternalId());
            this.startTime = System.currentTimeMillis() + delayInMilliseconds;
            this.startTimeWindowBegin = this.startTime - windowTimeMs;
            this.startTimeWindowEnd = this.startTime + windowTimeMs;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = startTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        /**
         * Use compareTo as a way to fudge equality.
         * <p>
         * The DelayQueue supports de-duplication of events by checking if an "equal" event
         * is already in the queue.
         * <p>
         * TeamCity sometimes generates the same events a few milliseconds apart. We can use
         * this to build a window of time where events for the same project and with a timestamp
         * of plus or minus 1000ms from ours can be treated as "the same".
         * <p>
         * The DelayQueue won't allow events to be emitted from the queue until they timeout, 
         * and we have set the timeout at 10 seconds. So a flood of events over a 2 second range will 
         * be de-duped to 1, and then after 10 seconds our thread will handle it as a single event. 
         */
        @Override
        public int compareTo(Delayed o) {
            DelayEvent e = (DelayEvent)o;
            if (e.getTypeAndEvent().equals(this.getTypeAndEvent())) {
                // Same type and data.
                // Check if the time from o falls within the window we would consider to be an overlap (plus or minus 1000 ms)  
                if (this.startTimeWindowBegin < e.startTime && this.startTimeWindowEnd > e.startTime) {
                    Loggers.SERVER.debug("WebHookProjectSettingsReloadTask :: Ignoring duplicate event for project " + e.getTypeAndEvent());
                    return 0; // Pretend it the same event because it's in our window.
                } else {
                    // If it doesn't fall in our window, use the difference in time as a compare point.
                    return Ints.saturatedCast(
                        this.startTime - ((DelayEvent) o).startTime);
                }
            } else {
                // Use the difference in TypeAndEvent as the compare point.
                return this.typeAndEvent.compareTo(e.getTypeAndEvent());
            }
        }

        @Override
        public WebHookSettingsEventType getEventType() {
            return this.event.getEventType();
        }
        @Override
        public Object getBaggage() {
            return this.event.getBaggage();
        }

        @Override
        public String getProjectInternalId() {
            return this.event.getProjectInternalId();
        }

        @Override
        public String getBuildTypeInternalId() {
            return this.event.getBuildTypeInternalId();
        }

    }
}
