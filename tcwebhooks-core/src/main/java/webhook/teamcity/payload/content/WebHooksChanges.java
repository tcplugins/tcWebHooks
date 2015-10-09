package webhook.teamcity.payload.content;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.vcs.SVcsModification;

public class WebHooksChanges extends ArrayList<WebHooksChange>{
	
	
	private static final long serialVersionUID = 1L;
	
	public static WebHooksChanges build (List<SVcsModification> mods){
		if (mods.isEmpty()){
			return null;
		}
		
		WebHooksChanges changes = new WebHooksChanges();
		
		for (SVcsModification modification: mods){
			changes.add(WebHooksChange.build(modification));
		}
		return changes;
	}
	
}
