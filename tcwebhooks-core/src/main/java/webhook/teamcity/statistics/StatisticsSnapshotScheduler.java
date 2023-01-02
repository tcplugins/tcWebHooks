package webhook.teamcity.statistics;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDateTime;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import webhook.teamcity.DeferrableService;
import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.settings.WebHookMainSettings;

public class StatisticsSnapshotScheduler implements DeferrableService {
	
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
		Loggers.SERVER.info("StatisticsSnapshotScheduler :: Registering as a deferrable service");
		myDeferrableServiceManager.registerService(this);
	}

	@Override
	public void register() {
		if (this.myWebHookMainSettings.isAssembleStatisticsEnabled()) {
			Loggers.SERVER.info("StatisticsSnapshotScheduler :: Requesting 60 minute scheduling of StatisticsUpdaterScheduledTask");
			this.myExecutorService.scheduleAtFixedRate(new StatisticsUpdaterScheduledTask(this, statisticsManager, false), 1, 60, TimeUnit.MINUTES);
		} else {	
			Loggers.SERVER.info("StatisticsSnapshotScheduler :: Statistics assembly is disabled. Not scheduling StatisticsUpdaterScheduledTask");
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
			Loggers.SERVER.debug("StatisticsUpdaterScheduledTask :: Starting task");
			if (this.isShutdownHook || ! this.myStatisticsSnapshotScheduler.shuttingDown) {
				LocalDateTime now = LocalDateTime.now();
				try {
					this.myStatisticsManager.updateStatistics(now);
					this.myStatisticsSnapshotScheduler.lastRun = now;
					if (! this.myStatisticsSnapshotScheduler.shuttingDown) {
						this.myStatisticsManager.reportStatistics(now);
						this.myStatisticsManager.cleanupOldStatistics(now);
					}
					Loggers.SERVER.debug("StatisticsUpdaterScheduledTask :: Completed task");
				} catch (Exception ex) {
					Loggers.SERVER.warn(String.format("StatisticsUpdaterScheduledTask :: Unable to update WebHook Statistics file for date '%s'", now), ex);
				}
			} else {
				Loggers.SERVER.debug(String.format("StatisticsUpdaterScheduledTask :: Completed task. No action required [isShutdownHook=%s,shuttingDown=%s]", this.isShutdownHook, this.myStatisticsSnapshotScheduler.shuttingDown));
			}
		}
	}
}
