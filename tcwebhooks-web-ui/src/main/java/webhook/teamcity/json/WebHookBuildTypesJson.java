package webhook.teamcity.json;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class WebHookBuildTypesJson {
	private boolean allEnabled;
	private boolean subProjectsEnabled;
	private Set<String> id;

}
