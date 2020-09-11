package webhook.teamcity.settings.project;

import static webhook.teamcity.settings.project.WebHookParameterFactory.NAME_KEY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import webhook.teamcity.Loggers;
import webhook.teamcity.TeamCityCoreFacade;

public class WebHookParameterStoreImpl implements WebHookParameterStore {

    private static final String PROJECT_FEATURE_TYPE = "tcWebHookParameter";

    private final TeamCityCoreFacade teamCityCore;

    public WebHookParameterStoreImpl(@NotNull TeamCityCoreFacade teamCityCore) {
        this.teamCityCore = teamCityCore;
    }
    
    @Override
    public WebHookParameter addWebHookParameter(@NotNull String projectInternalId, @NotNull WebHookParameter webhookParameter) {
        Map<String, String> params = WebHookParameterFactory.asMap(webhookParameter);
        SProject sProject = teamCityCore.findProjectByIntId(projectInternalId);
        SProjectFeatureDescriptor featureDescriptor = sProject.addFeature(PROJECT_FEATURE_TYPE, params);
        teamCityCore.persist(projectInternalId, "WebHookParameter added");
        Loggers.SERVER.info("WebHookParameter '" + webhookParameter.getName() + "' is created in the project " + projectInternalId);
        return fromProjectFeature(featureDescriptor);
    }

    @NotNull
    @Override
    public List<WebHookParameter> getAllWebHookParameters(@NotNull SProject project) {
    	return fromProjectFeatures(project.getAvailableFeaturesOfType(PROJECT_FEATURE_TYPE));
    }
    @NotNull
    @Override
    public List<WebHookParameter> getOwnWebHookParameters(@NotNull SProject project) {
    	return fromProjectFeatures(project.getOwnFeaturesOfType(PROJECT_FEATURE_TYPE));
    }

    @Override
    public WebHookParameter removeWebHookParameter(@NotNull String projectInternalId, @NotNull WebHookParameter webhookParameter) {
        SProject sProject = teamCityCore.findProjectByIntId(projectInternalId);
        return removeWebHookParameter(sProject, webhookParameter.getName());
    }
    
    @Override
    public WebHookParameter removeWebHookParameter(@NotNull SProject project, @NotNull String name) {
		SProjectFeatureDescriptor feature = filterFeatureDescriptors(project.getOwnFeaturesOfType(PROJECT_FEATURE_TYPE), name);
		if (feature != null) {
			project.removeFeature(feature.getId());
			teamCityCore.persist(project.getProjectId(), "WebHookParameter removed");
			return fromProjectFeature(feature);
		}
    	return null;
    }

    @Override
    public boolean updateWebHookParameter(@NotNull String projectInternalId, @NotNull WebHookParameter webhookParameter, @NotNull String description) {
    	SProject project = teamCityCore.findProjectByIntId(projectInternalId);
    	if (project == null) {
    		return false;
    	}
		SProjectFeatureDescriptor feature = project.findFeatureById(webhookParameter.getId());
		if (feature == null) {
			Loggers.SERVER.warn("WebHookParameterStoreImpl :: Unable to find existing WebHookParameter instance "
					+ " to update with ID: " + webhookParameter.getName());
			return false;
		}

		Map<String, String> params = WebHookParameterFactory.asMap(webhookParameter);
		project.updateFeature(feature.getId(), PROJECT_FEATURE_TYPE, params);
		teamCityCore.persist(project.getProjectId(), description);
		return true;
    }

    private List<WebHookParameter> fromProjectFeatures(Collection<SProjectFeatureDescriptor> features) {
    	List<WebHookParameter> params = new ArrayList<>();
    	for (SProjectFeatureDescriptor feature : features) {
    		params.add(fromProjectFeature(feature));
    	}
    	return params;
    }
    private WebHookParameter fromProjectFeature(SProjectFeatureDescriptor feature) {
        return WebHookParameterFactory.readFrom(feature.getId(), feature.getParameters());
    }

	@Override
	public WebHookParameter getWebHookParameter(SProject sProject, String parameterName) {
		SProjectFeatureDescriptor feature = filterFeatureDescriptors(sProject.getOwnFeaturesOfType(PROJECT_FEATURE_TYPE), parameterName);
		return feature != null ? fromProjectFeature(feature) : null; 
	}
	
	@Override
	public WebHookParameter getWebHookParameterById(SProject sProject, String parameterId) {
		SProjectFeatureDescriptor feature = sProject.findFeatureById(parameterId);
		return feature != null ? fromProjectFeature(feature) : null; 
	}

	@Override
	public WebHookParameter findWebHookParameter(SProject sProject, String parameterName) {
		SProjectFeatureDescriptor feature = filterFeatureDescriptors(sProject.getAvailableFeaturesOfType(PROJECT_FEATURE_TYPE), parameterName);
		return feature != null ? fromProjectFeature(feature) : null; 
	}
	
	@Nullable
	private SProjectFeatureDescriptor filterFeatureDescriptors(Collection<SProjectFeatureDescriptor> features, String parameterName) {
    	for (SProjectFeatureDescriptor feature : features) {
    		if (feature.getParameters().containsKey(NAME_KEY) && feature.getParameters().get(NAME_KEY).equals(parameterName)){
    			return feature;
    		}
    	}
        return null;
	}
}