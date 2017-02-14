package webhook.teamcity;

import com.intellij.openapi.diagnostic.Logger;

public final class Loggers {
    private Loggers() {
    }

    public static final Logger SERVER = Logger.getInstance("jetbrains.buildServer.SERVER");
    public static final Logger ACTIVITIES = Logger.getInstance("jetbrains.buildServer.ACTIVITIES");
}
