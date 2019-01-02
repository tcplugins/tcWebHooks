package webhook.teamcity.settings;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class WebHookSearchFilter {

	@Builder.Default String show = null;
	@Builder.Default String textSearch = null;
	@Builder.Default String webhookId = null;
	@Builder.Default String templateId = null;
	@Builder.Default String formatShortName = null;
	@Builder.Default String urlSubString = null;
	@Builder.Default String projectExternalId = null;
	@Builder.Default String buildTypeExternalId = null;
	@Builder.Default Set<String> tags = new HashSet<>();

	public void addTag(String tag) {
		if (! tag.isEmpty() ) {
			this.tags.add(tag.toLowerCase());
		}
	}

	public WebHookSearchFilter templateId(String templateId) {
		this.templateId = templateId;
		return this;
	}

}
