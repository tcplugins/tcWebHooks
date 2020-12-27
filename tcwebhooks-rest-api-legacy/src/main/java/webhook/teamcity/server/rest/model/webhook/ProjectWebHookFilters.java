package webhook.teamcity.server.rest.model.webhook;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookFilterConfig;

@XmlRootElement(name = "filters")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "filters", propOrder = { "count", "filters", "href", "prevHref", "nextHref" })
@Getter
@Setter
@NoArgsConstructor // empty constructor for JAXB
public class ProjectWebHookFilters {

	@XmlAttribute
	Integer count = 0;

	@XmlAttribute
	String href;

	@XmlElement(name="filter") @Getter
	List<ProjectWebHookFilter> filters = new ArrayList<>();

	@XmlAttribute(required = false)
	@Nullable
	public String nextHref;

	@XmlAttribute(required = false)
	@Nullable
	public String prevHref;

	public ProjectWebHookFilters(@NotNull final WebHookConfig config, @NotNull final List<WebHookFilterConfig> filterConfigs, @NotNull String projectExternalId,
			@Nullable final PagerData pagerData, final @NotNull Fields fields, @NotNull final BeanContext beanContext) {
		for(WebHookFilterConfig filter : filterConfigs ) {
			count++;
			ProjectWebHookFilter newFilter = ProjectWebHookFilter.copy(filter, count, beanContext.getApiUrlBuilder().getWebHookFilterHref(projectExternalId, config, count));
			filters.add(newFilter);
		}
		count = filters.size();
		href = ValueWithDefault.decideDefault(fields.isIncluded("href"), beanContext.getApiUrlBuilder().getWebHookFiltersHref(projectExternalId, config));
	}

	public List<WebHookFilterConfig> getFilterConfigs() {
		List<WebHookFilterConfig> filterConfigs = new ArrayList<>();
		for (ProjectWebHookFilter filter : this.filters) {
			filterConfigs.add(WebHookFilterConfig.create(filter.getValue(), filter.getRegex(), filter.isEnabled()));
		}
		return filterConfigs;
	}
}
