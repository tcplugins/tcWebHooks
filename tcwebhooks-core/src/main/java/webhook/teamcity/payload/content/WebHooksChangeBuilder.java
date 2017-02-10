package webhook.teamcity.payload.content;

import jetbrains.buildServer.vcs.SVcsModification;

import java.util.ArrayList;
import java.util.List;

public class WebHooksChangeBuilder {
    private WebHooksChangeBuilder() {
    }

    public static List<WebHooksChanges> build(List<SVcsModification> mods) {
        List<WebHooksChanges> changes = new ArrayList<>();

        for (SVcsModification modification : mods) {
            changes.add(new WebHooksChanges(modification.getDisplayVersion(), WebHooksChange.build(modification)));
        }
        return changes;
    }

}
