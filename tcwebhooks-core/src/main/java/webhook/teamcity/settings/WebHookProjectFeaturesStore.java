package webhook.teamcity.settings;

import static java.util.stream.Collectors.toList;

import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.ProjectsModelListener;
import jetbrains.buildServer.serverSide.ProjectsModelListenerAdapter;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.util.EventDispatcher;
import webhook.teamcity.Loggers;
public class WebHookProjectFeaturesStore implements WebHookFeaturesStore {
    
    
    private static final String PROJECT_FEATURE_TYPE = "tcWebHooks";
    private final @NotNull ProjectFeatureToWebHookConfigConverter configConverter = new ProjectFeatureToWebHookConfigConverter();

    
//    public WebHookProjectFeaturesStore(
//            @NotNull EventDispatcher<ProjectsModelListener> events) {
//        super();
//        events.addListener(new ProjectsModelListenerAdapter() {
//            @Override
//            public void projectFeatureAdded(@NotNull SProject project, @NotNull SProjectFeatureDescriptor projectFeature) {
//                //resetCache();
//            }
//
//            @Override
//            public void projectFeatureRemoved(@NotNull SProject project, @NotNull SProjectFeatureDescriptor projectFeature) {
//                //resetCache();
//            }
//
//            @Override
//            public void projectFeatureChanged(@NotNull SProject project, @NotNull SProjectFeatureDescriptor before, @NotNull SProjectFeatureDescriptor after) {
//                //resetCache();
//            }
//        });
//
//    }
    
    
//    Map<String, String> params = ChatClientConfigFactory.asMap(chatClientConfig);
//    SProject sProject = teamCityCore.findProjectByIntId(chatClientConfig.getProjectInternalId());
//    sProject.addFeature(PROJECT_FEATURE_TYPE, params);
//    teamCityCore.persist(chatClientConfig.getProjectInternalId(), "ChatClientConfig added");
//    Loggers.SERVER.info("ChatClientConfig " + chatClientConfig.getClientType() + " : " + chatClientConfig.getConfigId() + " is created in the project " + chatClientConfig.getProjectInternalId());
//    getChatClientConfig(chatClientConfig.getConfigId());//populate cache
//    return chatClientConfig;
//}


    @Override
    public WebHookUpdateResult addWebHookConfig(@NotNull SProject sProject, @NotNull WebHookConfig webHookConfig) {
        Map<String, String> params = configConverter.convert(webHookConfig).getParameters();
        sProject.addFeature(PROJECT_FEATURE_TYPE, params);
        return new WebHookUpdateResult(true, webHookConfig);
    }

    @Override
    @NotNull
    public WebHookProjectSettings getWebHookConfigs(@NotNull SProject project) {
    	return new WebHookProjectSettings(project.getOwnFeaturesOfType(PROJECT_FEATURE_TYPE)
                      .stream()
                      .map(configConverter::convert)
                      .collect(toList()));
    }
    
    @Override
    public WebHookUpdateResult deleteWebHook(@NotNull SProject sProject, @NotNull String configId) {
        Optional<SProjectFeatureDescriptor> featureDescriptor = sProject.getOwnFeaturesOfType(PROJECT_FEATURE_TYPE).stream()
                .filter(feature -> feature.getParameters().get(ProjectFeatureToWebHookConfigConverter.ID_KEY).equals(configId))
                .findFirst();

        if (featureDescriptor.isPresent()) {
            sProject.removeFeature(featureDescriptor.get().getId());
    		return new WebHookUpdateResult(true, configConverter.convert(featureDescriptor.get()));
        } else {
            return new WebHookUpdateResult(false, null);
        }
    }
    
    @Override
    public WebHookUpdateResult deleteWebHook(@NotNull SProject sProject, @NotNull WebHookConfig config) {
    	Optional<SProjectFeatureDescriptor> featureDescriptor = sProject.getOwnFeaturesOfType(PROJECT_FEATURE_TYPE).stream()
    			.filter(feature -> feature.getParameters().get(ProjectFeatureToWebHookConfigConverter.ID_KEY).equals(config.getUniqueKey()))
    			.findFirst();
    	
    	if (featureDescriptor.isPresent()) {
    		sProject.removeFeature(featureDescriptor.get().getId());
    		return new WebHookUpdateResult(true, configConverter.convert(featureDescriptor.get()));
    	} else {
            return new WebHookUpdateResult(false, config);
    	}
    }

    @Override
    public WebHookUpdateResult updateWebHookConfig(@NotNull SProject sProject, @NotNull WebHookConfig config) {
        Optional<SProjectFeatureDescriptor> featureDescriptor = sProject.getOwnFeaturesOfType(PROJECT_FEATURE_TYPE).stream()
                .filter(feature -> feature.getParameters().get(ProjectFeatureToWebHookConfigConverter.ID_KEY).equals(config.getUniqueKey()))
                .findFirst();

        if (featureDescriptor.isPresent()) {
            Map<String, String> params = configConverter.convert(config).getParameters();
            sProject.updateFeature(featureDescriptor.get().getId(), PROJECT_FEATURE_TYPE, params);
            return new WebHookUpdateResult(true, config);
        } else {
            Loggers.SERVER.warn("WebHookProjectFeaturesStore :: Unable to find existing config instance "
                    + "of type '" + config.getUniqueKey() + "' to update with ID: " + config.getUniqueKey());
            return new WebHookUpdateResult(false, config);
        }
    }

}
