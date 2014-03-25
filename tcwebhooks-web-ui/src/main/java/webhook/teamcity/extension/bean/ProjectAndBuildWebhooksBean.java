package webhook.teamcity.extension.bean;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;

public class ProjectAndBuildWebhooksBean {
	SProject project;
	WebHookProjectSettings webHookProjectSettings;
	List<WebHookConfig> projectWebhooks;
	List<BuildWebhooksBean> buildWebhooks;
	
	public static ProjectAndBuildWebhooksBean newInstance (SProject project, WebHookProjectSettings settings, SBuildType sBuild) {
		ProjectAndBuildWebhooksBean bean = new ProjectAndBuildWebhooksBean();
		bean.project = project;
		bean.webHookProjectSettings = settings;
		
		bean.projectWebhooks = settings.getProjectWebHooksAsList();
		bean.buildWebhooks = new ArrayList<BuildWebhooksBean>();
		
		if (sBuild != null && sBuild.getProjectId().equals(project.getProjectId())){
			bean.buildWebhooks.add(new BuildWebhooksBean(sBuild, settings.getBuildWebHooksAsList(sBuild)));
		}
		return bean;
	}

	public int getProjectWebhookCount(){
		return this.projectWebhooks.size();
	}

	public int getBuildWebhookCount(){
		return this.buildWebhooks.size();
	}
	
	public SProject getProject() {
		return project;
	}

	public WebHookProjectSettings getWebHookProjectSettings() {
		return webHookProjectSettings;
	}

	public List<WebHookConfig> getProjectWebhooks() {
		return projectWebhooks;
	}

	public List<BuildWebhooksBean> getBuildWebhooks() {
		return buildWebhooks;
	}
	
	public String getExternalProjectId(){
		return TeamCityIdResolver.getExternalProjectId(project);
	}

}
