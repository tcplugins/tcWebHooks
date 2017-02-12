package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;

public class SlackComWebHookTemplate extends AbstractPropertiesBasedWebHookTemplate implements WebHookTemplate {

    public SlackComWebHookTemplate(WebHookTemplateManager manager) {
        super(manager);
    }

    String CONF_PROPERTIES = "webhook/teamcity/payload/template/SlackComWebHookTemplate.properties";


    @Override
    public String getTemplateDescription() {
        return "Slack.com JSON templates";
    }

    @Override
    public String getTemplateToolTipText() {
        return "Supports the slack.com JSON webhooks endpoint";
    }

    @Override
    public String getTemplateShortName() {
        return "slack.com";
    }

    @Override
    public boolean supportsPayloadFormat(String payloadFormat) {
        return payloadFormat.equalsIgnoreCase(WebHookPayloadJsonTemplate.FORMAT_SHORT_NAME);
    }

    @Override
    public String getLoggingName() {
        return "SlackComWebHookTemplate";
    }

    @Override
    public String getPropertiesFileName() {
        return CONF_PROPERTIES;
    }

    /**
     * Returning an empty string should let SimpleDateFormat
     * choose a locale relevant format.
     */
    @Override
    public String getPreferredDateTimeFormat() {
        return "";
    }

}
