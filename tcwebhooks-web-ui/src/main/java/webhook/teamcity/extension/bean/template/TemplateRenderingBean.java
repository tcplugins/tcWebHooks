package webhook.teamcity.extension.bean.template;

public class TemplateRenderingBean {

    String projectId;
    String webhookTemplateName;
    String webhookPayloadName;

    String webhookTemplate;
    String webhookTemplateRendered;


    public static TemplateRenderingBean build(String projectId, String template, String payload, String templateSource, String templateRendered) {
        TemplateRenderingBean t = new TemplateRenderingBean();
        t.projectId = projectId;
        t.webhookTemplateName = template;
        t.webhookPayloadName = payload;
        t.webhookTemplate = templateSource;
        t.webhookTemplateRendered = templateRendered;
        return t;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getWebhookTemplateName() {
        return webhookTemplateName;
    }

    public String getWebhookPayloadName() {
        return webhookPayloadName;
    }

    public String getWebhookTemplate() {
        return webhookTemplate;
    }

    public String getWebhookTemplateRendered() {
        return webhookTemplateRendered;
    }

}
