package webhook.teamcity.settings;

import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import lombok.AllArgsConstructor;
import webhook.teamcity.Loggers;

@AllArgsConstructor
public class WebHookProjectFeaturesStore implements WebHookFeaturesStore {
    
    private static final String PROJECT_FEATURE_TYPE = "tcWebHooks";
    private final @NotNull ProjectFeatureToWebHookConfigConverter configConverter;
    private final FeatureDescriptorSorter featureDescriptorSorter = new FeatureDescriptorSorter();
    
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
                      .sorted(featureDescriptorSorter)
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
    public class FeatureDescriptorSorter implements Comparator <SProjectFeatureDescriptor> {
        private final int PROJECT_FEATURE_PREFIX_LENGTH = "PROJECT_EXT_".length();

        @Override
        public int compare(SProjectFeatureDescriptor o1, SProjectFeatureDescriptor o2) {
            try {
                //Try to compare based on the integer value after "PROJECT_EXT_".
                // eg, 10 in "PROJECT_EXT_10"
                int o1Id = Integer.valueOf(o1.getId().substring(PROJECT_FEATURE_PREFIX_LENGTH));
                int o2Id = Integer.valueOf(o2.getId().substring(PROJECT_FEATURE_PREFIX_LENGTH));
                return Integer.compare(o1Id, o2Id);
            } catch (NumberFormatException ex) {
                // If that fails, just compare the text strings as that's the default behaviour in TeamCity anyway. 
                return o1.getId().compareTo(o2.getId());
            }
        }
    }
}
