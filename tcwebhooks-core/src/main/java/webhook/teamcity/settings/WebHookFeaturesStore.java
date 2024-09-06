package webhook.teamcity.settings;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.SProject;

public interface WebHookFeaturesStore {

    WebHookUpdateResult addWebHookConfig(@NotNull SProject sProject, @NotNull WebHookConfig webHookConfig);

    @NotNull WebHookProjectSettings getWebHookConfigs(@NotNull SProject project);

    WebHookUpdateResult deleteWebHook(@NotNull SProject sProject, @NotNull String configId);
    WebHookUpdateResult deleteWebHook(@NotNull SProject sProject, @NotNull WebHookConfig webHookConfig);

    WebHookUpdateResult updateWebHookConfig(@NotNull SProject sProject, @NotNull WebHookConfig config);

}
