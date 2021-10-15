package webhook.teamcity.server.rest.model.parameter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.project.WebHookParameter;

@Getter @Setter @NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="projectParameter")
@XmlType( propOrder = { "id", "name", "value", "secure", "includedInLegacyPayloads", "forceResolveTeamCityVariable", "templateEngine", "href" })
public class ProjectWebhookParameter implements WebHookParameter {
	
	@XmlAttribute
	private String id;

	@XmlElement
	private String name;
	
	@XmlElement
	private String value;
	
	@XmlElement
	private Boolean secure;
	
	@XmlElement
	private Boolean includedInLegacyPayloads;

	@XmlElement
	private Boolean forceResolveTeamCityVariable;
	
	@XmlElement
	private String templateEngine;
	
	@XmlAttribute
	private String href;
	
	public ProjectWebhookParameter(WebHookParameter parameter, String projectExternalId, Fields fields, BeanContext beanContext) {
		this.id = ValueWithDefault.decideDefault(fields.isIncluded("id", true, true), parameter.getId());
		this.name = ValueWithDefault.decideDefault(fields.isIncluded("name", true, true), parameter.getName());
		this.value = ValueWithDefault.decideDefault(fields.isIncluded("value", false, true), parameter.getValue());
		this.secure = ValueWithDefault.decideDefault(
				fields.isIncluded("secure", false, true),
				Boolean.TRUE.equals(parameter.getSecure()) // true if defined and true, else false
			);
		this.includedInLegacyPayloads = ValueWithDefault.decideDefault(
				fields.isIncluded("includedInLegacyPayloads", false, true),
				Boolean.TRUE.equals(parameter.getIncludedInLegacyPayloads()) // true if defined and true, else false
			);
		this.forceResolveTeamCityVariable = ValueWithDefault.decideDefault(
				fields.isIncluded("forceResolveTeamCityVariable", false, true),
				Boolean.TRUE.equals(parameter.getForceResolveTeamCityVariable()) // true if defined and true, else false
				);
		this.templateEngine = ValueWithDefault.decideDefault(fields.isIncluded("templateEngine", false, true), parameter.getTemplateEngine());
		href = ValueWithDefault.decideDefault(fields.isIncluded("href"), beanContext.getApiUrlBuilder().getHref(projectExternalId, parameter));

	}

}
