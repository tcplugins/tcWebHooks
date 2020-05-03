package webhook.teamcity.testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.CustomMessageTemplate;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookFilterConfig;
import webhook.teamcity.settings.WebHookHeaderConfig;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

public class WebHookConfigFactoryImpl implements WebHookConfigFactory {

	private final SBuildServer myServer;
	private final WebHookSettingsManager myWebHookSettingsManager;
	private final WebHookTemplateManager myWebHookTemplateManager;

	public WebHookConfigFactoryImpl(
			SBuildServer sBuildServer,
			WebHookSettingsManager projectSettingsManager,
			WebHookTemplateManager webHookTemplateManager

			) {
		myServer = sBuildServer;
		myWebHookSettingsManager = projectSettingsManager;
		myWebHookTemplateManager = webHookTemplateManager;
	}

	@Override
	public WebHookConfig build(WebHookExecutionRequest webHookExecutionRequest) {

		if (webHookExecutionRequest.getUniqueKey().equals("new")) {

			return buildNewConfig(webHookExecutionRequest);
		} else {
			try {
				// Find the existing, and override anything we got from the request.
				// This means that customTemplate, Parameters and filters will be copied over.
				WebHookConfig webHookConfig = findWebHookWithId(webHookExecutionRequest.getProjectExternalId(), webHookExecutionRequest.getUniqueKey());
				webHookConfig.setUrl(webHookExecutionRequest.getUrl());
				webHookConfig.setPayloadTemplate(webHookExecutionRequest.getTemplateId());
				webHookConfig.setAuthEnabled(webHookExecutionRequest.isAuthEnabled());
				webHookConfig.setAuthType(webHookExecutionRequest.getAuthType());
				webHookConfig.setAuthPreemptive(webHookExecutionRequest.isAuthPreemptive());
				webHookConfig.setAuthParameters(getAuthParameters(webHookExecutionRequest.getAuthParameters()));
				webHookConfig.setBuildStates(buildStates(webHookExecutionRequest.getConfigBuildStates()));

				return webHookConfig;

			} catch (WebHookConfigNotFoundException e) {
				return buildNewConfig(webHookExecutionRequest);
			}

		}
	}

	private Map<String, String> getAuthParameters(Map<String, String> authParameters) {
		Map<String,String> params = new HashMap<>();
		if (authParameters == null) {
			return params;
		}
		return authParameters;
	}

	private WebHookConfig buildNewConfig(WebHookExecutionRequest webHookExecutionRequest) {
		return WebHookConfig.builder()
					 .url(webHookExecutionRequest.getUrl())
					 .payloadTemplate(webHookExecutionRequest.getTemplateId())
					 .templates(new TreeMap<String,CustomMessageTemplate>())
					 .authEnabled(webHookExecutionRequest.isAuthEnabled())
					 .authType(webHookExecutionRequest.getAuthType())
					 .authPreemptive(webHookExecutionRequest.isAuthPreemptive())
					 .authParameters(getAuthParameters(webHookExecutionRequest.getAuthParameters()))
					 .filters(new ArrayList<WebHookFilterConfig>())
					 .states(buildStates(webHookExecutionRequest.getConfigBuildStates()))
					 .extraParameters(new TreeMap<String,String>())
					 .build();
	}

	private BuildState buildStates(Map<BuildStateEnum, Boolean> configBuildStates) {
		BuildState buildState = new BuildState();
		for (Map.Entry<BuildStateEnum, Boolean> entry : configBuildStates.entrySet()) {
			buildState.setEnabled(entry.getKey(), entry.getValue());
		}
		return buildState;
	}

	@Override
	public WebHookConfig build(WebHookTemplateExecutionRequest webHookExecutionRequest) throws WebHookConfigNotFoundException {
		return findWebHookWithId(webHookExecutionRequest.getProjectExternalId(), webHookExecutionRequest.getUniqueKey()).copy();
	}

	private WebHookConfig findWebHookWithId(String projectExternalId, String webHookConfigUniqueId) throws WebHookConfigNotFoundException {
		SProject myProject = myServer.getProjectManager().findProjectByExternalId(projectExternalId);
		for (SProject project : myProject.getProjectPath()){
			WebHookProjectSettings projSettings = (WebHookProjectSettings) myWebHookSettingsManager.getSettings(project.getProjectId());
		    	if (projSettings.isEnabled()){
			    	for (WebHookConfig whc : projSettings.getWebHooksConfigs()){
			    		if (whc.isEnabledForSubProjects() == false && !myProject.getProjectId().equals(project.getProjectId())){
			    			// Sub-projects are disabled and we are a subproject.
			    			if (Loggers.SERVER.isDebugEnabled()){
				    			Loggers.SERVER.debug(this.getClass().getSimpleName() + ":getListOfEnabledWebHooks() "
				    					+ ":: subprojects not enabled. myProject is: " + myProject.getProjectId() + ". webhook project is: " + project.getProjectId());
			    			}
			    			continue;
			    		}

			    		if (whc.getUniqueKey().equals(webHookConfigUniqueId)) {
							if (myWebHookTemplateManager.isRegisteredTemplate(whc.getPayloadTemplate())){
								return whc.copy();
							} else {
								throw new WebHookConfigNotFoundException("No registered Template " + whc.getPayloadTemplate());
							}
						}
			    	}
		    	} else {
		    		Loggers.SERVER.debug("WebHookUserRequestedExecutorImpl :: WebHooks are disasbled for  " + projectExternalId);
		    	}
		}
    	throw new WebHookConfigNotFoundException(String.format("Webhook Configuration %s was not found", webHookConfigUniqueId));
	}

	@Override
	public WebHookConfig buildSimple(WebHookTemplateExecutionRequest webHookTemplateExecutionRequest) {
		return WebHookConfig.builder()
				 .url(webHookTemplateExecutionRequest.getUrl())
				 .payloadFormat(webHookTemplateExecutionRequest.getFormat())
				 .templates(new TreeMap<String,CustomMessageTemplate>())
				 .headers(new ArrayList<WebHookHeaderConfig>())
				 .extraParameters(new TreeMap<String,String>()) //TODO: Should we get from config somehow?
				 .build();
	}

}
