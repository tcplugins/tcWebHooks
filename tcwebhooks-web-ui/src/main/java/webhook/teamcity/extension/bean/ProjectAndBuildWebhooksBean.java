package webhook.teamcity.extension.bean;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.extension.bean.BuildWebhooksBean.WebHookConfigWithGeneralisedAddressWrapper;
import webhook.teamcity.history.GeneralisedWebAddress;
import webhook.teamcity.history.GeneralisedWebAddressType;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;

public class ProjectAndBuildWebhooksBean {
	SProject project;
	boolean isAdmin;
	WebHookProjectSettings webHookProjectSettings;
	List<WebHookConfigWithGeneralisedAddressWrapper> projectWebhooksWithAddress;
	List<BuildWebhooksBean> buildWebhooks;

	public static ProjectAndBuildWebhooksBean newInstance (SProject project, WebHookProjectSettings settings, SBuildType sBuild, boolean isAdmin, WebAddressTransformer webAddressTransformer) {
		ProjectAndBuildWebhooksBean bean = new ProjectAndBuildWebhooksBean();
		bean.project = project;
		bean.isAdmin = isAdmin;
		bean.webHookProjectSettings = settings;
		bean.projectWebhooksWithAddress = new ArrayList<>();

		for (WebHookConfig c : settings.getProjectWebHooksAsList()) {
			bean.projectWebhooksWithAddress.add(new WebHookConfigWithGeneralisedAddressWrapper(
					c, getGeneralisedWebAddress(webAddressTransformer, c.getUrl())
				));
		}

		bean.buildWebhooks = new ArrayList<>();

		if (sBuild != null && sBuild.getProjectId().equals(project.getProjectId())){
			bean.buildWebhooks.add(new BuildWebhooksBean(sBuild, settings.getBuildWebHooksAsList(sBuild), webAddressTransformer));
		}
		return bean;
	}

	public static ProjectAndBuildWebhooksBean newInstance (SProject project, List<WebHookConfig> webHooks, boolean isAdmin, WebAddressTransformer webAddressTransformer) {
		ProjectAndBuildWebhooksBean bean = new ProjectAndBuildWebhooksBean();
		bean.project = project;
		bean.isAdmin = isAdmin;
		bean.projectWebhooksWithAddress = new ArrayList<>();
		
		for (WebHookConfig c : webHooks) {
			bean.projectWebhooksWithAddress.add(new WebHookConfigWithGeneralisedAddressWrapper(
					c, getGeneralisedWebAddress(webAddressTransformer, c.getUrl())
					));
		}
		
		return bean;
	}

	public int getProjectWebhookCount(){
		return this.projectWebhooksWithAddress.size();
	}

	public int getBuildWebhookCount(){
		return this.buildWebhooks.size();
	}

	public SProject getProject() {
		return project;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public WebHookProjectSettings getWebHookProjectSettings() {
		return webHookProjectSettings;
	}

	public List<WebHookConfigWithGeneralisedAddressWrapper> getProjectWebhooks() {
		return projectWebhooksWithAddress;
	}

	public List<BuildWebhooksBean> getBuildWebhooks() {
		return buildWebhooks;
	}

	public String getExternalProjectId(){
		return TeamCityIdResolver.getExternalProjectId(project);
	}

	public String getExternalId(){
		return TeamCityIdResolver.getExternalProjectId(project);
	}

	public String getSensibleProjectName(){
		if (project.getProjectId().equals("_Root")) {
			return project.getProjectId();
		}
		return project.getName();
	}

	private static GeneralisedWebAddress getGeneralisedWebAddress(WebAddressTransformer webAddressTransformer, String uri) {
		if (webAddressTransformer != null) {
			URL url = null;
			try {
				url = new URL(uri);
			} catch (MalformedURLException e) {
				Loggers.SERVER.warn("BuildWebhooksBean :: Could not build URL from '" + url + "'" );
				try {
					url = new URL("http://unknown");
				} catch (MalformedURLException e1) {
					// won't fail because we hard-code it to 'http://unknown'
				}
			}

			return webAddressTransformer.getGeneralisedHostName(url);
		}
		return GeneralisedWebAddress.build(uri, GeneralisedWebAddressType.DOMAIN_NAME);
	}

}
