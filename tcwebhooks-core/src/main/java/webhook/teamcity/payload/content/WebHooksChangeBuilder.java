package webhook.teamcity.payload.content;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.vcs.SVcsModification;

public class WebHooksChangeBuilder{
	private WebHooksChangeBuilder(){}
	
	public static List<WebHooksChanges> build (List<SVcsModification> mods){
		List<WebHooksChanges> changes = new ArrayList<>();
		
		for (SVcsModification modification: mods){
			changes.add(new WebHooksChanges(modification.getDisplayVersion(), WebHooksChange.build(modification)));
		}
		return changes;
	}
	
}
