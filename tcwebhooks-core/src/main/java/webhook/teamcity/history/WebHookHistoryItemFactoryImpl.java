package webhook.teamcity.history;

import java.net.MalformedURLException;
import java.net.URL;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import webhook.WebHookExecutionStats;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.settings.WebHookConfig;

public class WebHookHistoryItemFactoryImpl implements WebHookHistoryItemFactory {
	
	private final WebAddressTransformer myWebAddressTransformer;
	private final ProjectManager myProjectManager;
	
	public WebHookHistoryItemFactoryImpl(WebAddressTransformer webAddressTransformer, ProjectManager projectManager) {
		myWebAddressTransformer = webAddressTransformer;
		myProjectManager = projectManager;
	}
	
	public WebHookHistoryItem getWebHookHistoryItem(WebHookConfig whc, 
													WebHookExecutionStats webHookExecutionStats, 
													SBuild sBuild, 
													WebHookErrorStatus errorStatus) {
		WebHookHistoryItem item =  new WebHookHistoryItem(whc, webHookExecutionStats, sBuild, errorStatus);
		addGeneralisedWebAddress(whc, item);
		addSProject(item);
		addBuildTypeData(item);
		return item;
		
	}
	
	@Override
	public WebHookHistoryItem getWebHookHistoryTestItem(WebHookConfig whc,
			WebHookExecutionStats webHookExecutionStats, SBuild sBuild, WebHookErrorStatus errorStatus) {
		WebHookHistoryItem item =  new WebHookHistoryItem(whc, webHookExecutionStats, sBuild, errorStatus);
		addGeneralisedWebAddress(whc, item);
		addSProject(item);
		addBuildTypeData(item);
		item.setTest(true);
		return item;
	}
	

	@Override
	public WebHookHistoryItem getWebHookHistoryItem(WebHookConfig whc, WebHookExecutionStats webHookExecutionStats,
			SBuildType sBuildType, WebHookErrorStatus errorStatus) {
		WebHookHistoryItem item =  new WebHookHistoryItem(whc, webHookExecutionStats, sBuildType, errorStatus);
		addGeneralisedWebAddress(whc, item);
		addSProject(item);
		addBuildTypeData(item);
		return item;
	}
	
	@Override
	public WebHookHistoryItem getWebHookHistoryTestItem(WebHookConfig whc, WebHookExecutionStats webHookExecutionStats,
			SBuildType buildType, WebHookErrorStatus errorStatus) {
		WebHookHistoryItem item =  new WebHookHistoryItem(whc, webHookExecutionStats, buildType, errorStatus);
		addGeneralisedWebAddress(whc, item);
		addSProject(item);
		addBuildTypeData(item);
		item.setTest(true);
		return item;
	}

	@Override
	public WebHookHistoryItem getWebHookHistoryItem(WebHookConfig whc, WebHookExecutionStats webHookExecutionStats,
			SProject project, WebHookErrorStatus errorStatus) {
		WebHookHistoryItem item =  new WebHookHistoryItem(whc, webHookExecutionStats, project, errorStatus);
		addGeneralisedWebAddress(whc, item);
		addSProject(item);
		addBuildTypeData(item);
		return item;
	}
	

	@Override
	public WebHookHistoryItem getWebHookHistoryTestItem(WebHookConfig whc, WebHookExecutionStats webHookExecutionStats,
			SProject sProject, WebHookErrorStatus errorStatus) {
		WebHookHistoryItem item =  new WebHookHistoryItem(whc, webHookExecutionStats, sProject, errorStatus);
		addGeneralisedWebAddress(whc, item);
		addSProject(item);
		addBuildTypeData(item);
		item.setTest(true);
		return item;
	}
	
	private void addGeneralisedWebAddress(WebHookConfig whc, WebHookHistoryItem item) {
		try {
			if (item.getWebHookExecutionStats().getUrl() != null) {
				URL url = new URL(item.getWebHookExecutionStats().getUrl());
				item.setGeneralisedWebAddress(myWebAddressTransformer.getGeneralisedHostName(url));
			} else {
				URL url = new URL(whc.getUrl());
				item.setGeneralisedWebAddress(myWebAddressTransformer.getGeneralisedHostName(url));
			}
		} catch (MalformedURLException ex) {
			item.setGeneralisedWebAddress(null);
		}
	}
	
	private void addSProject(WebHookHistoryItem item) {
		item.setProjectName(myProjectManager.findProjectById(item.getProjectId()).getName());
	}
	
	private void addBuildTypeData(WebHookHistoryItem item) {
		if (item.getBuildTypeId() != null) {
			item.setBuildTypeName(myProjectManager.findBuildTypeById(item.getBuildTypeId()).getName());
			item.setBuildTypeExternalId(myProjectManager.findBuildTypeById(item.getBuildTypeId()).getExternalId());
		}
	}

}
