package webhook.teamcity.extension.bean;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;

public class ProjectWebHooksBean {
	String projectId;
	List<WebhookConfigAndBuildTypeListHolder> webHookList;
	
	
	public static ProjectWebHooksBean build(WebHookProjectSettings projSettings, SProject project){
		ProjectWebHooksBean bean = new ProjectWebHooksBean();
		
		bean.projectId = TeamCityIdResolver.getInternalProjectId(project);
		
		bean.webHookList = new ArrayList<WebhookConfigAndBuildTypeListHolder>();
		for (WebHookConfig config : projSettings.getWebHooksAsList()){
			WebhookConfigAndBuildTypeListHolder holder = new WebhookConfigAndBuildTypeListHolder(config);
    		for (SBuildType sBuildType : project.getBuildTypes()){
    			holder.addWebHookBuildType(new WebhookBuildTypeEnabledStatusBean(
    													sBuildType.getBuildTypeId(), 
    													sBuildType.getName(), 
    													config.isEnabledForBuildType(sBuildType)
    													)
    										);
    		}
    		bean.webHookList.add(holder);
		}
		
		return bean;
		
	}
}
