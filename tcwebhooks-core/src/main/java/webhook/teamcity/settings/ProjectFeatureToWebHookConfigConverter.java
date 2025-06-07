package webhook.teamcity.settings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

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
    private static final String BUILDS_ENABLED_KEY = "buildTypes";
    private static final String BUILDIDS_KEY = "buildTypeIds";
    private static final String ALL_PROJECT_BUILDS_ENABLED_VALUE = "allProjectBuilds";
    private static final String SELECTED_PROJECT_BUILDS_ENABLED_KEY = "selectedProjectBuilds";
    private static final String SUB_PROJECT_BUILDS_ENABLED_KEY = "subProjectBuilds";
    private static final String HIDE_SECURE_VALUES_KEY = "hideSecureValues";
    private static final String TRIGGER_FILTER_PREFIX = "triggerFilter_";
    private static final String HEADER_PREFIX = "header_";
    private static final String NAME_SUFFIX = "_name";
    private static final String VALUE_SUFFIX = "_value";
    private static final String WEBHOOK_PARAMETER_KEY_PREFIX = "parameter_";
    private static final String AUTHENTICATION_KEY = "authentication";
    private WebHookAuthenticatorProvider myWebHookAuthenticatorProvider;
    

    public ProjectFeatureToWebHookConfigConverter(WebHookAuthenticatorProvider authenticatorProvider) {
        this.myWebHookAuthenticatorProvider = authenticatorProvider;
    }
    
    public WebHookConfig convert(SProjectFeatureDescriptor projectFeatureDescriptor) {
        Map<String, String> parameters = projectFeatureDescriptor.getParameters();
        WebHookConfigBuilder builder = WebHookConfig.builder();
        populateSimpleParameters(builder, parameters);
        populateBuildTyepIds(builder, parameters.get(BUILDIDS_KEY));
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
            parameters.entrySet().stream()
            .filter(e -> e.getKey().startsWith(HEADER_PREFIX) && e.getKey().endsWith(NAME_SUFFIX))
            .map(e -> mapHeader(e, parameters))
            .collect(Collectors.toList())
        );
    }
    
    private void populateTriggerFilters(WebHookConfigBuilder builder, Map<String, String> parameters) {
        builder.filters(
            parameters.entrySet().stream()
            .filter(e -> e.getKey().startsWith(TRIGGER_FILTER_PREFIX) && e.getKey().endsWith(VALUE_SUFFIX))
            .map(e -> mapTriggerFilter(e, parameters))
            .collect(Collectors.toList())
            );
    }
    
    private void populateWebHookParameters(WebHookConfigBuilder builder, Map<String, String> parameters) {
        ExtraParameters extraParameters = new ExtraParameters();
        AtomicInteger counter = new AtomicInteger(1);
        parameters.entrySet().stream()
        	.filter(e -> e.getKey().startsWith(WEBHOOK_PARAMETER_KEY_PREFIX) && e.getKey().endsWith(NAME_SUFFIX))
        	.forEach(p -> {
        		extraParameters.add(mapParameter(p, parameters, counter.getAndIncrement()));
        });
        if (!extraParameters.isEmpty()) {
            builder.extraParameters(extraParameters);
        }
    }

    private static WebHookHeaderConfig mapHeader(Map.Entry<String, String> e, Map<String,String> parameters) {
    	String valueKey = e.getKey().replaceFirst(NAME_SUFFIX, VALUE_SUFFIX);
    	String value = parameters.get(valueKey);
    	return new WebHookHeaderConfig(e.getValue(), value);
    }
    
    private static WebHookFilterConfig mapTriggerFilter(Map.Entry<String, String> e, Map<String,String> parameters) {
    	WebHookFilterConfig filter = new WebHookFilterConfig();
    	filter.setValue(e.getValue());
    	filter.setRegex(parameters.get(e.getKey().replaceFirst(VALUE_SUFFIX, "_regex")));
    	filter.setEnabled(Boolean.parseBoolean(parameters.get(e.getKey().replaceFirst(VALUE_SUFFIX, "_enabled"))));
    	return filter;
    }
    private static WebHookParameterModel mapParameter(Map.Entry<String, String> e, Map<String,String> parameters, int id) {
    	
    	/*
            parameters.feature.param("parameter_${parameterCounter}_name", p.name)
            parameters.feature.param("parameter_${parameterCounter}_value", p.value)
            p.secure?.let { parameters.feature.param("parameter_${parameterCounter}_secure", p.secure.toString()) }
            p.includedInLegacyPayloads?.let { parameters.feature.param("parameter_${parameterCounter}_includedInLegacyPayloads", p.includedInLegacyPayloads.toString()) }
            p.forceResolveTeamCityVariable?.let { parameters.feature.param("parameter_${parameterCounter}_forceResolveTeamCityVariable", p.forceResolveTeamCityVariable.toString()) }
            p.templateEngine?.let { parameters.feature.param("parameter_${parameterCounter}_templateEngine", p.templateEngine.toString()) }

    	 */
    	
    	
    	WebHookParameterModel parameter = new WebHookParameterModel();
    	parameter.setContext(ExtraParameters.WEBHOOK);
    	String valueKey = e.getKey().replaceFirst(NAME_SUFFIX, VALUE_SUFFIX);
    	String secureKey = e.getKey().replaceFirst(NAME_SUFFIX, "_secure");
    	String includedInLegacyPayloadsKey = e.getKey().replaceFirst(NAME_SUFFIX, "_includedInLegacyPayloads");
    	String forceResolveTeamCityVariableKey = e.getKey().replaceFirst(NAME_SUFFIX, "_forceResolveTeamCityVariable");
    	String templateEngineKey = e.getKey().replaceFirst(NAME_SUFFIX, "_templateEngine");
    	
    	parameter.setId(String.valueOf(id));
    	parameter.setName(e.getValue());
    	parameter.setValue(parameters.get(valueKey));
    	if (parameters.containsKey(secureKey)) {
    		parameter.setSecure(Boolean.valueOf(parameters.get(secureKey)));
    	}
    	if (parameters.containsKey(includedInLegacyPayloadsKey)) {
    		parameter.setIncludedInLegacyPayloads(Boolean.valueOf(parameters.get(includedInLegacyPayloadsKey)));
    	}
    	if (parameters.containsKey(forceResolveTeamCityVariableKey)) {
    		parameter.setForceResolveTeamCityVariable(Boolean.valueOf(parameters.get(forceResolveTeamCityVariableKey)));
    	}
    	if (parameters.containsKey(templateEngineKey)) {
    		parameter.setTemplateEngine(parameters.get(templateEngineKey));
    	}
    	return parameter;
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
        return new WebHookProjectFeature(webhook.getUniqueKey(), webhook, myWebHookAuthenticatorProvider).init();
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
        if (parameters.containsKey(BUILDS_ENABLED_KEY) && parameters.get(BUILDS_ENABLED_KEY).equals(ALL_PROJECT_BUILDS_ENABLED_VALUE)) {
            builder.allBuildTypesEnabled(true);
        } else {
            builder.allBuildTypesEnabled(false);
        }
        builder.subProjectsEnabled(Boolean.valueOf(parameters.getOrDefault(SUB_PROJECT_BUILDS_ENABLED_KEY, TRUE_VALUE)));
        builder.hideSecureValues(Boolean.valueOf(parameters.getOrDefault(HIDE_SECURE_VALUES_KEY, TRUE_VALUE)));
    }

    public static class WebHookProjectFeature implements SProjectFeatureDescriptor {
        
        private static final String DISABLED = "disabled";
        private String id;
        private WebHookConfig webHookConfig;
        private WebHookAuthenticatorProvider myWebHookAuthenticatorProvider;
        private Map<String,String> parameters;

        public WebHookProjectFeature(String id, WebHookConfig webHookConfig, WebHookAuthenticatorProvider webHookAuthenticatorProvider) {
            this.id = id;
            this.webHookConfig = webHookConfig;
            this.myWebHookAuthenticatorProvider = webHookAuthenticatorProvider;
        }

        public SProjectFeatureDescriptor init() {
            this.parameters = initParameters();
            return this;
        }

        @Override
        public String getType() {
            return "tcWebHook";
        }
        
        @Override
        public Map<String, String> getParameters() {
        	return this.parameters;
        }

        public Map<String, String> initParameters() {
        	Map<String,String> parameters = new LinkedHashMap<>(); // use LinkedHashMap so that insertion order is maintained.
            parameters.put(ID_KEY, this.webHookConfig.getUniqueKey());
            parameters.put(URL_KEY, this.webHookConfig.getUrl());
            parameters.put(TEMPLATE_KEY, this.webHookConfig.getPayloadTemplate());
            parameters.put(ENABLED_KEY, this.webHookConfig.getEnabled().toString());
            if (! this.webHookConfig.isHideSecureValues()) {
                parameters.put(HIDE_SECURE_VALUES_KEY, Boolean.toString(this.webHookConfig.isHideSecureValues()));
            }
            if (this.webHookConfig.isEnabledForAllBuildsInProject()) {
                parameters.put(BUILDS_ENABLED_KEY, ALL_PROJECT_BUILDS_ENABLED_VALUE);
            } else {
                parameters.put(BUILDS_ENABLED_KEY, SELECTED_PROJECT_BUILDS_ENABLED_KEY);
            }
            parameters.put(SUB_PROJECT_BUILDS_ENABLED_KEY, this.webHookConfig.isEnabledForSubProjects().toString());
            if (CollectionUtils.isNotEmpty(this.webHookConfig.getEnabledBuildTypesSet())) {
                parameters.put(BUILDIDS_KEY, this.webHookConfig.getEnabledBuildTypesSet().stream().collect(Collectors.joining(",")));
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
                parameters.put(TRIGGER_FILTER_PREFIX + count + "_value", triggerFilter.getValue());
                parameters.put(TRIGGER_FILTER_PREFIX + count + "_regex", triggerFilter.getRegex());
                parameters.put(TRIGGER_FILTER_PREFIX + count + "_enabled", Boolean.toString(triggerFilter.isEnabled()));
                count++;
            }
        }

        private void addHeaders(Map<String, String> parameters) {
            int count = 0;
            for (WebHookHeaderConfig header : this.webHookConfig.getHeaders()) {
                parameters.put(HEADER_PREFIX + count + "_name", header.name);
                parameters.put(HEADER_PREFIX + count + "_value", header.value);
                count++;
            }
        }

        /**
         * Adds WebHookParameters from the {@link WebHookConfig} into the ProjectFeature parameters map
         * @param parameters
         */
        private void addWebHookParameters(Map<String, String> parameters) {
            int count = 0;
            if (this.webHookConfig.getParams() != null) {
                for (WebHookParameterModel parameter : this.webHookConfig.getParams()) {
                    parameters.put(WEBHOOK_PARAMETER_KEY_PREFIX + count + "_name", parameter.getName());
                    parameters.put(WEBHOOK_PARAMETER_KEY_PREFIX + count + "_value", parameter.getValue());
                    if (parameter.getSecure() != null) {
                    	parameters.put(WEBHOOK_PARAMETER_KEY_PREFIX + count + "_secure", Boolean.toString(parameter.getSecure()));
                    }
                    if (parameter.getIncludedInLegacyPayloads() != null) {
                    	parameters.put(WEBHOOK_PARAMETER_KEY_PREFIX + count + "_includedInLegacyPayloads", Boolean.toString(parameter.getIncludedInLegacyPayloads()));
                    }
                    if (parameter.getForceResolveTeamCityVariable() != null) {
                    	parameters.put(WEBHOOK_PARAMETER_KEY_PREFIX + count + "_forceResolveTeamCityVariable", Boolean.toString(parameter.getForceResolveTeamCityVariable()));
                    }
                    if (parameter.getTemplateEngine() != null) {
                    	parameters.put(WEBHOOK_PARAMETER_KEY_PREFIX + count + "_templateEngine", parameter.getTemplateEngine());
                    }
                    count++;
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
