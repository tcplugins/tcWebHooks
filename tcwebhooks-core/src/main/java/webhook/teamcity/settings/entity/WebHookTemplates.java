package webhook.teamcity.settings.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.settings.entity.WebHookTemplateEntity;

@XmlRootElement(name = "webhook-templates")
public class WebHookTemplates {
	@NotNull
	private List<WebHookTemplateEntity> templateList = new ArrayList<>();

	@NotNull
	@XmlElement(name = "webhook-template")
	public List<WebHookTemplateEntity> getWebHookTemplateList() {
		return templateList;
	}

	public void setWebHookTemplateList(
			@NotNull List<WebHookTemplateEntity> templateList) {
		this.templateList = templateList;
	}

	public void addWebHookTemplate(WebHookTemplateEntity template) {
		this.templateList.add(template);
	}
	
	public void addAllWebHookTemplates(Collection<WebHookTemplateEntity> templates) {
		this.templateList.addAll(templates);
	}
	

}
