package webhook.teamcity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import webhook.WebHook;

import jetbrains.buildServer.Build;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.notification.Notificator;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.UserPropertyInfo;
import jetbrains.buildServer.users.NotificatorPropertyKey;
import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.SUser;


public class WebHookNotificator implements Notificator {

    private static final String TYPE = "webHookNotifier";
    private static final String TYPE_NAME = "WebHook Notifier";
    private static final String WEBHOOK_URL = "webHookURL";

    private static final PropertyKey URL = new NotificatorPropertyKey(TYPE, WEBHOOK_URL);
    
    public WebHookNotificator(NotificatorRegistry notificatorRegistry) throws IOException {
        ArrayList<UserPropertyInfo> userProps = new ArrayList<UserPropertyInfo>();
        userProps.add(new UserPropertyInfo(WEBHOOK_URL, "WebHook URL"));
        notificatorRegistry.register(this, userProps);
    }

    public void notifyBuildStarted(SRunningBuild sRunningBuild, Set<SUser> sUsers) {
        doNotifications("Build " + sRunningBuild.getFullName() + " started.",sUsers);
    }

    public void notifyBuildSuccessful(SRunningBuild sRunningBuild, Set<SUser> sUsers) {
        doNotifications("Build " + sRunningBuild.getFullName() + " successfull.",sUsers);
    }

    public void notifyBuildFailed(SRunningBuild sRunningBuild, Set<SUser> sUsers) {
        doNotifications("Build " + sRunningBuild.getFullName() + " failed.",sUsers);
    }

    public void notifyLabelingFailed(Build build, jetbrains.buildServer.vcs.VcsRoot vcsRoot, Throwable throwable, Set<SUser> sUsers) {
        doNotifications("Labeling of build " + build.getFullName() + " failed.",sUsers);
    }

    public void notifyBuildFailing(SRunningBuild sRunningBuild, Set<SUser> sUsers) {
        doNotifications("Build " + sRunningBuild.getFullName() + " is failing.",sUsers);
    }

    public void notifyBuildProbablyHanging(SRunningBuild sRunningBuild, Set<SUser> sUsers) {
        doNotifications("Build " + sRunningBuild.getFullName() + " is probably hanging.",sUsers);
    }

    public void notifyResponsibleChanged(SBuildType sBuildType, Set<SUser> sUsers) {
        doNotifications("Responsibility of build " + sBuildType.getFullName() + " changed.",sUsers);
    }

    public String getNotificatorType() {
        return TYPE;
    }

    public String getDisplayName() {
        return TYPE_NAME;
    }

    public void doNotifications(String message, Set<SUser> sUsers) {
        for(SUser user : sUsers) {
        	Loggers.SERVER.info("WebHookNotifier :: " + message);
        	WebHook webhook = new WebHook(user.getPropertyValue(URL));
        	webhook.addParam("message", message);
        	try {
				webhook.post();
			} catch (FileNotFoundException e) {
				Loggers.SERVER.error(e.toString());
			} catch (IOException e) {
				Loggers.SERVER.error(e.toString());
			}
        }
    }
}
