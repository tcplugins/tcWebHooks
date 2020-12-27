package webhook.teamcity.server.rest.model.webhook;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookFilterConfig;
import webhook.teamcity.settings.WebHookHeaderConfig;

@XmlRootElement(name = "headers")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "headers", propOrder = { "count", "webhookId", "headers", "href", "prevHref", "nextHref" })
@Getter
@Setter
@NoArgsConstructor // empty constructor for JAXB
public class ProjectWebHookHeaders {

	@XmlAttribute
	Integer count = 0;

	@XmlAttribute
	String webhookId;

	@XmlAttribute
	String href;

	@XmlElement(name="header") @Getter
	List<ProjectWebHookHeader> headers = new ArrayList<>();

	@XmlAttribute(required = false)
	@Nullable
	public String nextHref;

	@XmlAttribute(required = false)
	@Nullable
	public String prevHref;

	public ProjectWebHookHeaders(@NotNull final WebHookConfig config, @NotNull final List<ProjectWebHookHeader> webhookFilters, @NotNull String projectExternalId,
			@Nullable final PagerData pagerData, final @NotNull Fields fields, @NotNull final BeanContext beanContext) {
		
	}

	public List<WebHookHeaderConfig> getHeaderConfigs() {
		return new ArrayList<WebHookHeaderConfig>(this.headers);
	}
}
