package webhook.teamcity.settings.entity;

import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "webhook-templates")
public class WebHookTemplates {
    @NotNull
    private List<WebHookTemplate> templateList = new ArrayList<>();

    @NotNull
    @XmlElement(name = "webhook-template")
    public List<WebHookTemplate> getWebHookTemplateList() {
        return templateList;
    }

    public void setWebHookTemplateList(
            @NotNull List<WebHookTemplate> templateList) {
        this.templateList = templateList;
    }

    public void addWebHookTemplate(WebHookTemplate template) {
        this.templateList.add(template);
    }

}