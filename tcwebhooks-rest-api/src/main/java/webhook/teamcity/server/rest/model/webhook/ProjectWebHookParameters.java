package webhook.teamcity.server.rest.model.webhook;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameter;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.project.WebHookParameter;

@XmlRootElement(name = "parameters")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parameters", propOrder = { "count", "webhookId", "parameters", "href", "prevHref", "nextHref" })
@Getter
@Setter
@NoArgsConstructor // empty constructor for JAXB
public class ProjectWebHookParameters {

	@XmlAttribute
	Integer count = 0;

	@XmlAttribute
	String webhookId;

	@XmlAttribute
	String href;

	@XmlElement(name="parameter") @Getter
	List<ProjectWebhookParameter> parameters = new ArrayList<>();

	@XmlAttribute(required = false)
	@Nullable
	public String nextHref;

	@XmlAttribute(required = false)
	@Nullable
	public String prevHref;

	public ProjectWebHookParameters(@NotNull final WebHookConfig config, @NotNull final List<WebHookParameter> webhookParameters, @NotNull String projectExternalId,
			@Nullable final PagerData pagerData, final @NotNull Fields fields, @NotNull final BeanContext beanContext) {
		
		//this.webhookId = ValueWithDefault.decideDefault(fields.isIncluded("webhookId", false, true), config.getUniqueKey());

		if (Boolean.TRUE.equals(fields.isIncluded("parameters", true, true))) {
			parameters = ValueWithDefault.decideIncludeByDefault(fields.isIncluded("parameters"),
					new ValueWithDefault.Value<List<ProjectWebhookParameter>>() {
						public List<ProjectWebhookParameter> get() {
							final ArrayList<ProjectWebhookParameter> result = new ArrayList<>(parameters.size());
							for (WebHookParameter parameter : webhookParameters) {
								result.add(new ProjectWebhookParameter(parameter, fields, beanContext.getApiUrlBuilder().getWebHookParameterHref(projectExternalId, config, parameter)));
							}
							return result;
						}
					});
			if (pagerData != null) {
				href = ValueWithDefault.decideDefault(fields.isIncluded("href"),
						beanContext.getApiUrlBuilder().transformRelativePath(pagerData.getHref()));
				nextHref = ValueWithDefault.decideDefault(fields.isIncluded("nextHref"),
						pagerData.getNextHref() != null
								? beanContext.getApiUrlBuilder().transformRelativePath(pagerData.getNextHref())
								: null);
				prevHref = ValueWithDefault.decideDefault(fields.isIncluded("prevHref"),
						pagerData.getPrevHref() != null
								? beanContext.getApiUrlBuilder().transformRelativePath(pagerData.getPrevHref())
								: null);
			}
			count = ValueWithDefault.decideIncludeByDefault(fields.isIncluded("count"), parameters.size());
		} else {
			parameters = null;
		}
	}

	public void addProjectParameter(ProjectWebhookParameter parameter) {
		this.parameters.add(parameter);
	}

}
