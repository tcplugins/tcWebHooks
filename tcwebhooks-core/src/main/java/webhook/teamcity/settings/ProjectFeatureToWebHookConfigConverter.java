package webhook.teamcity.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.auth.WebHookAuthenticatorFactory;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.exception.OperationUnsupportedException;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.settings.WebHookConfig.WebHookConfigBuilder;
import webhook.teamcity.settings.project.WebHookParameterModel;

public class ProjectFeatureToWebHookConfigConverter {
    /*
     * 
      <parameters>
        <param name="authentication" value="basicAuth" />
        <param name="basicAuthPassword" value="myPassword" />
        <param name="basicAuthPreemptive" value="true" />
        <param name="basicAuthRealm" value="myRealm" />
        <param name="basicAuthUsername" value="myUserName" />
        <param name="buildAddedToQueue" value="enabled" />
        <param name="buildRemovedFromQueue" value="enabled" />
        <param name="buildStates" value="enabledBuildStates" />
        <param name="buildTypeIds" value="RootProjectId_TcDummyDeb, RootProjectId_TcWebHooks" />
        <param name="buildTypes" value="selectedProjectBuilds" />
        <param name="subProjectBuilds" value="true" />
        <param name="template" value="legacy-json" />
        <param name="url" value="http://localhost:8111/webhooks/endpoint.html?vcs_test=1" />
        <param name="webHookId" value="SmallKotlinProject_WebHook_01" />
      </parameters>
      
      <parameters>
        <param name="authentication" value="bearer" />
        <param name="bearerPreemptive" value="true" />
        <param name="bearerToken" value="this-is-my-token" />
        <param name="buildAddedToQueue" value="enabled" />
        <param name="buildRemovedFromQueue" value="enabled" />
        <param name="buildStates" value="enabledBuildStates" />
        <param name="buildTypes" value="allProjectBuilds" />
        <param name="header_0_name" value="foo1" />
        <param name="header_0_value" value="bar1" />
        <param name="header_1_name" value="foo2" />
        <param name="header_1_value" value="bar2" />
        <param name="header_2_name" value="foo3" />
        <param name="header_2_value" value="bar3" />
        <param name="parameter_0_name" value="colour" />
        <param name="parameter_0_value" value="blue" />
        <param name="subProjectBuilds" value="true" />
        <param name="template" value="slack.com-compact" />
        <param name="url" value="http://localhost:8111/webhooks/endpoint.html?vcs_test=8" />
        <param name="webHookId" value="SmallKotlinProject_WebHook_08" />
      </parameters>
     *
     */
    
    private static final String ENABLED = "enabled";
    private static final String TRUE_VALUE = "true";
    static final String ID_KEY = "webHookId";
    private static final String URL_KEY = "url";
    private static final String ENABLED_KEY = ENABLED;
    private static final String TEMPLATE_KEY = "template";
    private static final String ENABLED_BUILDS_KEY = "enabledBuilds";
    private static final String ALL_PROJECT_BUILDS_ENABLED_KEY = "allProjectBuildsEnabled";
    private static final String SUB_PROJECT_BUILDS_ENABLED_KEY = "subProjectBuildsEnabled";
    private static final String HIDE_SECURE_VALUES_KEY = "hideSecureValues";
    private static final String TRIGGER_FILTER_KEY_PREFIX = "triggerFilter_";
    private static final String HEADER_KEY_PREFIX = "header_";
    private static final String WEBHOOK_PARAMETER_KEY_PREFIX = "parameter_";
    private static final String AUTHENTICATION_KEY = "authentication";
    
    private static final Gson gson = new GsonBuilder().create();
    private WebHookAuthenticatorProvider myWebHookAuthenticatorProvider;
    

    public ProjectFeatureToWebHookConfigConverter(WebHookAuthenticatorProvider authenticatorProvider) {
        this.myWebHookAuthenticatorProvider = authenticatorProvider;
    }
    
    public WebHookConfig convert(SProjectFeatureDescriptor projectFeatureDescriptor) {
        Map<String, String> parameters = projectFeatureDescriptor.getParameters();
        WebHookConfigBuilder builder = WebHookConfig.builder();
        populateSimpleParameters(builder, parameters);
        populateBuildTyepIds(builder, parameters.get(ENABLED_BUILDS_KEY));
        populateBuildStates(builder, parameters);
        builder.projectInternalId(projectFeatureDescriptor.getProjectId());
        populateAuthentication(builder, parameters);
        populateWebHookParameters(builder, parameters);
        populateTriggerFilters(builder, parameters);
        populateHeaders(builder, parameters);
        WebHookConfig config = builder.build();
        Loggers.SERVER.debug(String.format("ProjectFeatureToWebHookConfigConverter :: Assembled WebHookConfig from projectFeatureDescriptor. webHookConfig: '%s'", config));
        return config;
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
        if (!extraParameters.isEmpty()) {
            builder.extraParameters(extraParameters);
        }
    }

    /**
     * Add Authentication to the WebHookConfig by reading values from ProjectFeature Parameters
     * @param builder - {@link WebHookConfig}'s builder to add values to.
     * @param parameters - parameters from ProjectFeature
     */
    private void populateAuthentication(WebHookConfigBuilder builder, Map<String, String> parameters) {
        if (parameters.containsKey("authentication")) {
            WebHookAuthenticatorFactory authFactory = this.myWebHookAuthenticatorProvider.findAuthenticatorFactoryByProjectFeatureName(parameters.get("authentication"));
            if (authFactory != null) {
                builder.authEnabled(true)
                .authType(authFactory.getName());
                Map<String, String> authParams = new HashMap<>();
                authFactory.getParameterList().forEach(p -> {
                    if (parameters.containsKey(p.getProjectFeatureKey())) {
                        authParams.put(p.getKey(), parameters.get(p.getProjectFeatureKey()));
                    }
                });
                builder.authParameters(authParams);
                if (parameters.containsKey(authFactory.getProjectFeaturePrefix() + "Preemptive")) {
                    builder.authPreemptive(Boolean.valueOf(parameters.get(authFactory.getProjectFeaturePrefix() + "Preemptive")));
                }
            } else {
                Loggers.SERVER.warn("ProjectFeatureToWebHookConfigConverter :: unsupported authentication type: " + parameters.get("authentication"));
                throw new OperationUnsupportedException(String.format("Failed to convert ProjectFeature to WebHookConfig. No registered authentication type '%s'", parameters.get("authentication")));
            }
        }
        /*
        if (json != null) {
            WebHookAuthConfig authConfig = gson.fromJson(json, WebHookAuthConfig.class);
            builder.authEnabled(true)
            .authParameters(authConfig.getParameters())
            .authPreemptive(authConfig.getPreemptive())
            .authType(authConfig.getType());
        }
        */
    }
    public SProjectFeatureDescriptor convert(WebHookConfig webhook) {
        return new WebHookProjectFeature(webhook.getUniqueKey(), webhook, myWebHookAuthenticatorProvider);
    }

    private static void populateBuildStates(WebHookConfigBuilder builder, Map<String, String> parameters) {
        BuildState buildStates = new BuildState();
        Arrays.stream(BuildStateEnum.values())
            .forEach(s -> {
                if (parameters.containsKey(s.getShortName()) && parameters.get(s.getShortName()).equals(ENABLED)) {
                    buildStates.enable(s);
                }
            });
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
        
        private static final String DISABLED = "disabled";
        private String id;
        private WebHookConfig webHookConfig;
        private WebHookAuthenticatorProvider myWebHookAuthenticatorProvider;

        public WebHookProjectFeature(String id, WebHookConfig webHookConfig, WebHookAuthenticatorProvider webHookAuthenticatorProvider) {
            this.id = id;
            this.webHookConfig = webHookConfig;
            this.myWebHookAuthenticatorProvider = webHookAuthenticatorProvider;
        }

        @Override
        public String getType() {
            return "tcWebHook";
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
            addBuildStates(parameters);
            addTriggerFilters(parameters);
            addHeaders(parameters);
            addWebHookParameters(parameters);
            if (this.webHookConfig.getAuthEnabled().booleanValue()) {
                parameters.putAll(getAuthenticationAsParameters(this.webHookConfig.getAuthenticationConfig()));
            }
            return parameters;
        }


        private void addBuildStates(Map<String, String> parameters) {
            BuildState buildStates = this.webHookConfig.getBuildStates();
            Arrays.stream(BuildStateEnum.getNotifyStates()).forEach(state -> {
                parameters.put(state.getShortName(), buildStates.enabled(state) ? ENABLED : DISABLED);
            });
            parameters.put(BuildStateEnum.REPORT_STATISTICS.getShortName(), buildStates.enabled(BuildStateEnum.REPORT_STATISTICS) ? ENABLED : DISABLED);
        }

        private Map<String,String> getAuthenticationAsParameters(WebHookAuthConfig webHookAuthConfig) {
            Map<String,String> authParams = new HashMap<>();
            WebHookAuthenticatorFactory authFactory = myWebHookAuthenticatorProvider.findAuthenticatorFactoryByType(webHookAuthConfig.getType());
            if (authFactory != null) {
                authParams.put(AUTHENTICATION_KEY, authFactory.getProjectFeaturePrefix());
                authFactory.getParameterList().stream().forEach(p -> {
                    if (webHookAuthConfig.getParameters().containsKey(p.getKey())) {
                        authParams.put(p.getProjectFeatureKey(), webHookAuthConfig.getParameters().get(p.getKey()));
                    }
                });
                return authParams;
            } else {
                Loggers.SERVER.warn("ProjectFeatureToWebHookConfigConverter :: unsupported authentication type: " + webHookAuthConfig.getType());
                throw new OperationUnsupportedException(String.format("Failed to convert WebHookConfig to ProjectFeature. No registered authentication type '%s'", webHookAuthConfig.getType()));
            }
            //return gson.toJson(this.webHookConfig.getAuthenticationConfig());
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
            if (this.webHookConfig.getParams() != null) {
                for (WebHookParameterModel parameter : this.webHookConfig.getParams()) {
                    count++;
                    parameters.put(WEBHOOK_PARAMETER_KEY_PREFIX + count, gson.toJson(parameter));
                }
            }
        }

        @Override
        public String getProjectId() {
            return this.webHookConfig.getProjectInternalId();
        }

        @Override
        public String getId() {
            return this.id;
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
