package webhook.teamcity.settings;

import java.util.HashSet;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class WebHookSearchFilter {

	@Builder.Default String textSearch = null;
	@Builder.Default String webhookId = null;
	@Builder.Default String templateId = null;
	@Builder.Default String urlSubString = null;
	@Builder.Default Set<String> tags = new HashSet<String>();
	
	public void addTag(String tag) {
		if (! tag.isEmpty() ) {
			this.tags.add(tag.toLowerCase());
		}
	}
	
}
