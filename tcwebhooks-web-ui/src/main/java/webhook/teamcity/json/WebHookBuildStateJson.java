package webhook.teamcity.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import webhook.teamcity.BuildStateEnum;

@Data @NoArgsConstructor @AllArgsConstructor
public class WebHookBuildStateJson {

	private BuildStateEnum type;
	private boolean enabled;
}
