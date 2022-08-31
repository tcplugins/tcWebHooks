package webhook.teamcity.json;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebHookHeaderJson extends WebHookConfigurationListWrapper {
	private List<Header> header;

	@Data @AllArgsConstructor @NoArgsConstructor
	public static class Header {
		private String name;
		private String value;
	}
}
