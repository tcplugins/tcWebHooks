package webhook.teamcity.payload.content;

import jetbrains.buildServer.serverSide.TriggeredBy;
import jetbrains.buildServer.users.SUser;

public class WebHooksTriggeredBy {

    String name = "";
    String username = "";
    String email = "";

    public static WebHooksTriggeredBy build(TriggeredBy triggeredBy) {
        if (triggeredBy != null) {
            WebHooksTriggeredBy buildTriggeredBy = new WebHooksTriggeredBy();
            if (triggeredBy.getUser() != null) {
                SUser user = triggeredBy.getUser();
                buildTriggeredBy.name = user.getName();
                buildTriggeredBy.username = user.getUsername();
                buildTriggeredBy.email = user.getEmail();
                return buildTriggeredBy;
            } else {
                buildTriggeredBy.name = triggeredBy.getAsString();
                return buildTriggeredBy;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return name;
    }
}
