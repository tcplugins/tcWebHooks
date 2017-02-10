package webhook.teamcity.settings;

public interface WebHookConfigChangeHandler {
    public abstract void handleConfigFileChange();
}
