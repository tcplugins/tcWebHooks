package webhook.teamcity.statistics;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.intellij.openapi.diagnostic.Logger;
import org.joda.time.LocalDateTime;

import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import webhook.teamcity.DeferrableService;
import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.settings.WebHookMainSettings;

public class StatisticsSnapshotScheduler implements DeferrableService {
	private static final Logger LOG = Logger.getInstance(StatisticsSnapshotScheduler.class.getName());

	LocalDateTime lastRun = null;
	private final ScheduledExecutorService myExecutorService;
	private final DeferrableServiceManager myDeferrableServiceManager;
	private final StatisticsManager statisticsManager;
	private final WebHookMainSettings myWebHookMainSettings;

	private boolean shuttingDown = false;

	
	public StatisticsSnapshotScheduler(ExecutorServices executorServices, DeferrableServiceManager deferrableServiceManager, StatisticsManager statisticsManager, WebHookMainSettings webHookMainSettings) {
		this.myExecutorService = executorServices.getNormalExecutorService();
		this.myDeferrableServiceManager = deferrableServiceManager;
		this.statisticsManager = statisticsManager;
		this.myWebHookMainSettings = webHookMainSettings;
	}

	@Override
	public void requestDeferredRegistration() {
		LOG.info("StatisticsSnapshotScheduler :: Registering as a deferrable service");
		myDeferrableServiceManager.registerService(this);
	}

	@Override
	public void register() {
		if (this.myWebHookMainSettings.isAssembleStatisticsEnabled()) {
			LOG.info("StatisticsSnapshotScheduler :: Requesting 60 minute scheduling of StatisticsUpdaterScheduledTask");
			this.myExecutorService.scheduleAtFixedRate(new StatisticsUpdaterScheduledTask(this, statisticsManager, false), 1, 60, TimeUnit.MINUTES);
		} else {	
			LOG.info("StatisticsSnapshotScheduler :: Statistics assembly is disabled. Not scheduling StatisticsUpdaterScheduledTask");
		}
	}
	
	@Override
	public void unregister() {
		this.shuttingDown = true;
		if (this.myWebHookMainSettings.isAssembleStatisticsEnabled()) {
			new StatisticsUpdaterScheduledTask(this, statisticsManager, true).run();
		}
	}
	
	
	public class StatisticsUpdaterScheduledTask implements Runnable {
		
		private StatisticsSnapshotScheduler myStatisticsSnapshotScheduler;
		private StatisticsManager myStatisticsManager;
		private boolean isShutdownHook;

		public StatisticsUpdaterScheduledTask(StatisticsSnapshotScheduler statisticsSnapshotScheduler, StatisticsManager statisticsManager, boolean isShutdownHook) {
			this.myStatisticsSnapshotScheduler = statisticsSnapshotScheduler;
			this.myStatisticsManager = statisticsManager;
			this.isShutdownHook = isShutdownHook;
		}

		@Override
		public void run() {
			LOG.debug("StatisticsUpdaterScheduledTask :: Starting task");
			if (this.isShutdownHook || ! this.myStatisticsSnapshotScheduler.shuttingDown) {
				LocalDateTime now = LocalDateTime.now();
				try {
					this.myStatisticsManager.updateStatistics(now);
					this.myStatisticsSnapshotScheduler.lastRun = now;
					if (! this.myStatisticsSnapshotScheduler.shuttingDown) {
						this.myStatisticsManager.reportStatistics(now);
						this.myStatisticsManager.cleanupOldStatistics(now);
					}
					LOG.debug("StatisticsUpdaterScheduledTask :: Completed task");
				} catch (Exception ex) {
					LOG.warn(String.format("StatisticsUpdaterScheduledTask :: Unable to update WebHook Statistics file for date '%s'", now), ex);
				}
			} else {
				LOG.debug(String.format("StatisticsUpdaterScheduledTask :: Completed task. No action required [isShutdownHook=%s,shuttingDown=%s]", this.isShutdownHook, this.myStatisticsSnapshotScheduler.shuttingDown));
			}
		}
	}
}
