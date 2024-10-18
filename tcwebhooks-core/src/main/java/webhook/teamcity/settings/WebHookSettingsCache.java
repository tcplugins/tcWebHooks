package webhook.teamcity.settings;

import jetbrains.buildServer.serverSide.SProject;

public interface WebHookSettingsCache {
    
    public void refreshCache(SProject project);

}
