package webhook.teamcity.settings;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
public class WebHookSearchResult {
	private List<Match> matches;
	@Setter
	private WebHookConfigEnhanced webHookConfigEnhanced;
	
	public void addMatch(Match match) {
		this.matches.add(match);
	}

	public enum Match {
		TAG, URL, ID, TEMPLATE, PAYLOAD_FORMAT
	}
}
