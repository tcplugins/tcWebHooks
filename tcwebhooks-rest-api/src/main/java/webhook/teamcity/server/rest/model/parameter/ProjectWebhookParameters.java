package webhook.teamcity.server.rest.model.parameter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.util.WebHookBeanContext;
import webhook.teamcity.settings.project.WebHookParameter;

@XmlRootElement(name = "parameters")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parameters", propOrder = { "count", "projectId", "parameters", "href", "prevHref", "nextHref" })
@Getter
@Setter
@NoArgsConstructor // empty constructor for JAXB
public class ProjectWebhookParameters {

	@XmlAttribute
	Integer count = 0;

	@XmlAttribute
	String projectId;

	@XmlAttribute
	String href;

	@XmlElementWrapper(name="parameters") @Getter
	List<ProjectWebhookParameter> parameters = new ArrayList<>();

	@XmlAttribute(required = false)
	@Nullable
	public String nextHref;

	@XmlAttribute(required = false)
	@Nullable
	public String prevHref;

	public ProjectWebhookParameters(@NotNull final List<WebHookParameter> webhookParameters, @NotNull String projectExternalId,
			@Nullable final PagerData pagerData, final @NotNull Fields fields, @NotNull final WebHookApiUrlBuilder myWebHookApiUrlBuilder) {
		
		this.projectId = ValueWithDefault.decideDefault(fields.isIncluded("projectId", false, true), projectExternalId);

		if (Boolean.TRUE.equals(fields.isIncluded("parameters", true, true))) {
			final ArrayList<ProjectWebhookParameter> result = new ArrayList<>(parameters.size());
			for (WebHookParameter parameter : webhookParameters) {
				result.add(new ProjectWebhookParameter(parameter, fields, myWebHookApiUrlBuilder.getProjectParameterHref(projectExternalId, parameter)));
			}
			parameters = result;
			if (pagerData != null) {
				href = ValueWithDefault.decideDefault(fields.isIncluded("href"),
						myWebHookApiUrlBuilder.transformRelativePath(pagerData.getHref()));
				nextHref = ValueWithDefault.decideDefault(fields.isIncluded("nextHref"),
						pagerData.getNextHref() != null
								? myWebHookApiUrlBuilder.transformRelativePath(pagerData.getNextHref())
								: null);
				prevHref = ValueWithDefault.decideDefault(fields.isIncluded("prevHref"),
						pagerData.getPrevHref() != null
								? myWebHookApiUrlBuilder.transformRelativePath(pagerData.getPrevHref())
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
