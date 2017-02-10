package webhook.teamcity.payload.content;

public class WebHooksChanges {
    private String version;
    private WebHooksChange change;

    public WebHooksChanges(String version, WebHooksChange change) {
        this.version = version;
        this.change = change;
    }

    public String getVersion() {
        return version;
    }

    public WebHooksChange getChange() {
        return change;
    }
}
