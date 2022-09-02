package webhook.teamcity.json;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebHookHeaderJson extends WebHookConfigurationListWrapper {
	private List<Header> header;

	@Data @AllArgsConstructor @NoArgsConstructor
	public static class Header {
		private Integer id;
		private String name;
		private String value;
	}

	public static WebHookHeaderJson create(List<Header> headers) {
		WebHookHeaderJson webHookFilter = new WebHookHeaderJson();
		webHookFilter.setHeader(headers);
		webHookFilter.setCount(headers.size());
		return webHookFilter;
	}
}
