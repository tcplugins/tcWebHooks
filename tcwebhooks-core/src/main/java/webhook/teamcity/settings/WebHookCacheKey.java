package webhook.teamcity.settings;

import lombok.Data;
import lombok.NonNull;

@Data
public class WebHookCacheKey {
    @NonNull
    private String projectInternalId;
    @NonNull
    private String webhookId;
}
