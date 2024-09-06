package webhook.teamcity.settings;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.settings.WebHookConfig.WebHookConfigBuilder;
import webhook.teamcity.settings.project.WebHookParameterModel;

public class ProjectFeatureToWebHookConfigConverter {
    /*
     * 
     <parameters>
        <param name="enabled" value="true" />
        <param name="url" value="http://localhost:58001/200" />
        <param name="template" value="legacy-json" />
        <!-- enabledBuildStates can be a comma separated list of enabled build states -->
        <param name="enabledBuildStates" value="buildStarted,changesLoaded,beforeBuildFinished,buildInterrupted,buildSuccessful,buildFailed,responsiblityChanged" />
        <param name="parameter_0" value="{'name':'colour', 'value':'red'}" />
        <param name="parameter_1" value="{'name':'notify', 'value':'1'}"/>
        <param name="parameter_2" value="{'name':'branchName', 'value':'${branchDisplayName}', 'secure':false, included-in-legacy-payloads:true, 'force-resolve-teamcity-variable':true, 'template-engine':'VELOCITY'}" />
        <param name="authentication" value="{'type':'bearer', 'bearer':'${test.thing1}'}" />
        <param name="triggerFilter_0" value="{'value':'${branchDisplayName}', 'regex':'^master$', 'enabled':true}" />
        <param name="triggerFilter_1" value="{'value':'${buildInternalTypeId}', 'regex':'^bt\d$', 'enabled':true}" />
        <param name="allProjectBuildsEnabled" value="false" />
        <param name="subProjectBuildsEnabled" value="true" />
        <param name="enabledBuilds" value="bt1,bt2" />
      </parameters>
     *
     */
    
    private static final String TRUE_VALUE = "true";
    public static final String ID_KEY = "id";
    private static final String URL_KEY = "url";
    private static final String ENABLED_KEY = "enabled";
    private static final String TEMPLATE_KEY = "template";
    private static final String ENABLED_BUILDS_KEY = "enabledBuilds";
    private static final String ENABLED_BUILD_STATES_KEY = "enabledBuildStates";
    private static final String ALL_PROJECT_BUILDS_ENABLED_KEY = "allProjectBuildsEnabled";
    private static final String SUB_PROJECT_BUILDS_ENABLED_KEY = "subProjectBuildsEnabled";
    private static final String HIDE_SECURE_VALUES_KEY = "hideSecureValues";
    private static final String TRIGGER_FILTER_KEY_PREFIX = "triggerFilter_";
    private static final String HEADER_KEY_PREFIX = "header_";
    private static final String WEBHOOK_PARAMETER_KEY_PREFIX = "parameter_";
    private static final String AUTHENTICATION_KEY = "authentication";
    
    private static final Gson gson = new GsonBuilder().setExclusionStrategies().create();


    
    public WebHookConfig convert(SProjectFeatureDescriptor projectFeatureDescriptor) {
        Map<String, String> parameters = projectFeatureDescriptor.getParameters();
        WebHookConfigBuilder builder = WebHookConfig.builder();
        populateSimpleParameters(builder, parameters);
        populateBuildTyepIds(builder, parameters.get(ENABLED_BUILDS_KEY));
        populateBuildStates(builder, parameters.get(ENABLED_BUILD_STATES_KEY));
        builder.projectInternalId(projectFeatureDescriptor.getProjectId());
        populateAuthentication(builder, parameters.get(AUTHENTICATION_KEY));
        populateWebHookParameters(builder, parameters);
        populateTriggerFilters(builder, parameters);
        populateHeaders(builder, parameters);
        return builder.build();
    }
    
    public SProjectFeatureDescriptor convert(WebHookConfig webhook) {
        return new WebHookProjectFeature(webhook);
    }

    private void populateHeaders(WebHookConfigBuilder builder, Map<String, String> parameters) {
        builder.headers(
            parameters.entrySet().stream().filter(e -> e.getKey().startsWith(HEADER_KEY_PREFIX))
            .map(e -> gson.fromJson(e.getValue(), WebHookHeaderConfig.class))
            .collect(Collectors.toList())
        );
    }
    
    private void populateTriggerFilters(WebHookConfigBuilder builder, Map<String, String> parameters) {
        builder.filters(
                parameters.entrySet().stream().filter(e -> e.getKey().startsWith(TRIGGER_FILTER_KEY_PREFIX))
                .map(e -> gson.fromJson(e.getValue(), WebHookFilterConfig.class))
                .collect(Collectors.toList())
                );
    }



    private void populateWebHookParameters(WebHookConfigBuilder builder, Map<String, String> parameters) {
        ExtraParameters extraParameters = new ExtraParameters();
        parameters.entrySet().stream().filter(e -> e.getKey().startsWith(WEBHOOK_PARAMETER_KEY_PREFIX)).forEach(p -> {
            extraParameters.add(gson.fromJson(p.getValue(), WebHookParameterModel.class));
        });
        builder.extraParameters(extraParameters);
    }

    private void populateAuthentication(WebHookConfigBuilder builder, String json) {
        if (json != null) {
            WebHookAuthConfig authConfig = gson.fromJson(json, WebHookAuthConfig.class);
            builder.authEnabled(true)
            .authParameters(authConfig.getParameters())
            .authPreemptive(authConfig.getPreemptive())
            .authType(authConfig.getType());
        }
    }

    private static void populateBuildStates(WebHookConfigBuilder builder, String enabledBuildStates) {
        BuildState buildStates = new BuildState();
        if (enabledBuildStates != null) {
            Arrays.stream(enabledBuildStates.split(","))
            .forEach(s -> {
                BuildStateEnum state = BuildStateEnum.findBuildState(s.trim());
                if (state != null) {
                    buildStates.enable(state);
                }
            });
        }
        builder.states(buildStates);
    }

    private static void populateBuildTyepIds(WebHookConfigBuilder builder, String buildTypeIds) {
        if (buildTypeIds != null) {
            builder.enabledBuildTypesSet(Arrays.stream(buildTypeIds.split(",")).collect(Collectors.toSet()));
        }
    }

    private static void populateSimpleParameters(WebHookConfigBuilder builder, Map<String, String> parameters) {
        builder.uniqueKey(parameters.get(ID_KEY));
        builder.url(parameters.get(URL_KEY));
        builder.enabled(Boolean.valueOf(parameters.getOrDefault(ENABLED_KEY, TRUE_VALUE)));
        builder.payloadTemplate(parameters.get(TEMPLATE_KEY));
        builder.allBuildTypesEnabled(Boolean.valueOf(parameters.getOrDefault(ALL_PROJECT_BUILDS_ENABLED_KEY, TRUE_VALUE)));
        builder.subProjectsEnabled(Boolean.valueOf(parameters.getOrDefault(SUB_PROJECT_BUILDS_ENABLED_KEY, TRUE_VALUE)));
        builder.hideSecureValues(Boolean.valueOf(parameters.getOrDefault(HIDE_SECURE_VALUES_KEY, TRUE_VALUE)));
    }

    public static class WebHookProjectFeature implements SProjectFeatureDescriptor {
        
        private String id;
        private WebHookConfig webHookConfig;

        public WebHookProjectFeature(WebHookConfig webHookConfig) {
            this.id = webHookConfig.getUniqueKey();
            this.webHookConfig = webHookConfig;
        }

        @Override
        public String getType() {
            return "tcWebHook";
        }
        
        @Override
        public String getProjectId() {
            return this.webHookConfig.getProjectInternalId();
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public Map<String, String> getParameters() {
            Map<String,String> parameters = new LinkedHashMap<>(); // use LinkedHashMap so that insertion order is maintained.
            parameters.put(ID_KEY, this.webHookConfig.getUniqueKey());
            parameters.put(URL_KEY, this.webHookConfig.getUrl());
            parameters.put(TEMPLATE_KEY, this.webHookConfig.getPayloadTemplate());
            parameters.put(ENABLED_KEY, this.webHookConfig.getEnabled().toString());
            if (! this.webHookConfig.isHideSecureValues()) {
                parameters.put(HIDE_SECURE_VALUES_KEY, Boolean.toString(this.webHookConfig.isHideSecureValues()));
            }
            parameters.put(ALL_PROJECT_BUILDS_ENABLED_KEY, this.webHookConfig.isEnabledForAllBuildsInProject().toString());
            parameters.put(SUB_PROJECT_BUILDS_ENABLED_KEY, this.webHookConfig.isEnabledForSubProjects().toString());
            if (CollectionUtils.isNotEmpty(this.webHookConfig.getEnabledBuildTypesSet())) {
                parameters.put(ENABLED_BUILDS_KEY, this.webHookConfig.getEnabledBuildTypesSet().stream().collect(Collectors.joining(",")));
            }
            parameters.put(ENABLED_BUILD_STATES_KEY, getEnabledBuildStatesAsStrings());
            addTriggerFilters(parameters);
            addHeaders(parameters);
            addWebHookParameters(parameters);
            if (this.webHookConfig.getAuthEnabled().booleanValue()) {
                parameters.put(AUTHENTICATION_KEY, getAuthenticationAsJson());
            }
            return parameters;
        }


        private String getAuthenticationAsJson() {
            return gson.toJson(this.webHookConfig.getAuthenticationConfig());
        }

        private void addTriggerFilters(Map<String, String> parameters) {
            int count = 0;
            for (WebHookFilterConfig triggerFilter : this.webHookConfig.getTriggerFilters()) {
                count++;
                parameters.put(TRIGGER_FILTER_KEY_PREFIX + count, gson.toJson(triggerFilter));
            }
        }

        private void addHeaders(Map<String, String> parameters) {
            int count = 0;
            for (WebHookHeaderConfig header : this.webHookConfig.getHeaders()) {
                count++;
                parameters.put(HEADER_KEY_PREFIX + count, gson.toJson(header));
            }
        }

        private void addWebHookParameters(Map<String, String> parameters) {
            int count = 0;
            for (WebHookParameterModel parameter : this.webHookConfig.getParams()) {
                count++;
                parameters.put(WEBHOOK_PARAMETER_KEY_PREFIX + count, gson.toJson(parameter));
            }
        }

        private String getEnabledBuildStatesAsStrings() {
            return this.webHookConfig.getBuildStates().getEnabledBuildStates().stream().map(BuildStateEnum::getShortName).collect(Collectors.joining(","));
        }

    }
    
//    public static class WebHookParameterModelExclusionStrategy implements ExclusionStrategy {
//        @Override
//        public boolean shouldSkipClass(Class<?> clazz) {
//            return false;
//        }
//
//        @Override
//        public boolean shouldSkipField(FieldAttributes f) {
//            System.out.println(f.getDeclaringClass());
//           if (f.getDeclaringClass().isAssignableFrom(WebHookParameterModel.class) 
//                   && "forceResolveTeamCityVariable".equals(f.getName())
//                   && f.) {
//                return true;
//            }
//            return false;
//        }
//    }

}
