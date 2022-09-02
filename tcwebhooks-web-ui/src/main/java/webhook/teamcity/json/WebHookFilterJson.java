package webhook.teamcity.json;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebHookFilterJson extends WebHookConfigurationListWrapper {
	private List<Filter> filter;

	@Data @AllArgsConstructor @NoArgsConstructor
	public static class Filter {
		private Integer id;
		private String value;
		private String regex;
		private Boolean enabled;
	}
	
	public static WebHookFilterJson create(List<Filter> filters) {
		WebHookFilterJson webHookFilter = new WebHookFilterJson();
		webHookFilter.setFilter(filters);
		webHookFilter.setCount(filters.size());
		return webHookFilter;
	}
}
