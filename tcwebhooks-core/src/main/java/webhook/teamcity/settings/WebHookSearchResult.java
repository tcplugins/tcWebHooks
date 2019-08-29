package webhook.teamcity.settings;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
public class WebHookSearchResult {
	private List<Match> matches = new ArrayList<>();
	@Setter
	private WebHookConfigEnhanced webHookConfigEnhanced;
	@Setter
	private boolean filteredResult = false;

	public void addMatch(Match match) {
		this.matches.add(match);
	}

	public enum Match {
		SHOW, TAG, URL, ID, TEMPLATE, PAYLOAD_FORMAT, PROJECT, BUILD_TYPE
	}

	public WebHookConfig getWebHookConfig() {
		return this.webHookConfigEnhanced.getWebHookConfig();
	}
}
