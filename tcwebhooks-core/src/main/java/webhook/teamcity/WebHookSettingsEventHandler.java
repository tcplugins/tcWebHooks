package webhook.teamcity;

import lombok.AllArgsConstructor;
import lombok.Data;

public interface WebHookSettingsEventHandler {
    public void handleEvent(WebHookSettingsEvent event);
    
    public interface WebHookSettingsEvent {
        WebHookSettingsEventType getEventType();
        String getProjectInternalId();
        String getBuildTypeInternalId();
        Object getBaggage();
    }
    
    @Data
    @AllArgsConstructor
    public static class WebHookSettingsEventImpl implements WebHookSettingsEvent {
        WebHookSettingsEventType eventType;
        String projectInternalId;
        String buildTypeInternalId;
        Object baggage;
    }
}
