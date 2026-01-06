package webhook.teamcity.settings.converter;

import org.apache.commons.lang3.StringUtils;

import lombok.RequiredArgsConstructor;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.auth.WebHookAuthenticationParameter;
import webhook.teamcity.auth.WebHookAuthenticatorFactory;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookHeaderConfig;
import webhook.teamcity.settings.project.WebHookParameterModel;

@RequiredArgsConstructor
public class WebHookConfigToKotlinDslRenderer {
    private int leftPad = 8;
    static final int INDENT_SIZE_SPACES = 4;
    private final WebHookAuthenticatorProvider authenticatorProvider;
    private final BuildTypeIdResolver buildTypeIdResolver;
    /**
     * Renders the webhook as KotlinDSL. Allows setting the leftPad amount in case indentation is required.
     * For example:<code><pre>
      
       webHookConfiguration {
            webHookId = "MyProjectId_WebHook_01"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=1"
            buildTypes = selectedProjectBuilds {
                subProjectBuilds = true
                buildType{TcDummyDeb}
                buildType{TcWebHooks}
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = basic {
                username = "myUserName"
                password = "myPassword"
                realm = "myRealm"
                preemptive = true
            }
        }
      </pre></code>
     */
    public String renderAsKotlinDsl(WebHookConfig webHookConfig, int leftPad) {
        this.leftPad = leftPad;
        return renderAsKotlinDsl(webHookConfig);
    }
    public String renderAsKotlinDsl(WebHookConfig webHookConfig) {
        StringBuilder sb = new StringBuilder()
        .append(leftPad()).append("webHookConfiguration {\n")
        .append(leftPad(1)).append("webHookId = \"").append(webHookConfig.getUniqueKey()).append("\"\n")
        .append(leftPad(1)).append("template = \"").append(webHookConfig.getPayloadTemplate()).append("\"\n")
        .append(leftPad(1)).append("url = \"").append(webHookConfig.getUrl()).append("\"\n")
        .append(getBuildTypes(webHookConfig))
        .append(getBuildStates(webHookConfig))
        .append(getAuthentication(webHookConfig))
        .append(getHeaders(webHookConfig))
        .append(getParameters(webHookConfig))
        // TODO; Filters?
        .append(leftPad()).append("}");
        return sb.toString();
    }

    private String getAuthentication(WebHookConfig webHookConfig) {
        StringBuilder sb = new StringBuilder();
        WebHookAuthConfig authenticationConfig = webHookConfig.getAuthenticationConfig();
        if (authenticationConfig != null) {
            WebHookAuthenticatorFactory factory = this.authenticatorProvider.findAuthenticatorFactoryByType(authenticationConfig.getType());
            sb.append(leftPad(1)).append("authentication = ").append(factory.getKotlinDslName()).append(" {\n");
            for (WebHookAuthenticationParameter param : factory.getParameterList()) {
                if (authenticationConfig.getParameters().containsKey(param.getKey())) {
                    sb.append(leftPad(2)).append(param.getKotlinDslKey()).append(" = \"").append(escapeKotlinString(authenticationConfig.getParameters().get(param.getKey()))).append("\"\n");
                }
            }
            sb.append(leftPad(2)).append("preemptive = ").append(authenticationConfig.getPreemptive().toString()).append("\n")
            .append(leftPad(1)).append("}\n");
        }
        return sb.toString();
    }

    private String getBuildStates(WebHookConfig webHookConfig) {
        StringBuilder sb = new StringBuilder();
        sb.append(leftPad(1)).append("buildStates {\n");
        for (BuildStateEnum state : BuildStateEnum.getNotifyStates()) {
            if (webHookConfig.isEnabledForBuildState(state)) {
                sb.append(leftPad(2)).append(state.getShortName()).append(" = true\n");
            }
        }
        if (webHookConfig.isEnabledForBuildState(BuildStateEnum.REPORT_STATISTICS)) {
            sb.append(leftPad(2)).append(BuildStateEnum.REPORT_STATISTICS.getShortName()).append(" = true\n");
        }
        sb.append(leftPad(1)).append("}\n");
        return sb.toString();
    }

    private String getBuildTypes(WebHookConfig webHookConfig) {
        StringBuilder sb = new StringBuilder();
        if (Boolean.TRUE.equals(webHookConfig.isEnabledForAllBuildsInProject())) {
            sb.append(leftPad(1)).append("buildTypes = allProjectBuilds {\n")
              .append(leftPad(2)).append("subProjectBuilds = ").append(Boolean.toString(webHookConfig.isEnabledForSubProjects())).append("\n")
              .append(leftPad(1)).append("}\n");
        } else {
            sb.append(leftPad(1)).append("buildTypes = selectedProjectBuilds {\n")
              .append(leftPad(2)).append("subProjectBuilds = ").append(Boolean.toString(webHookConfig.isEnabledForSubProjects())).append("\n")
              .append(leftPad(2)).append("/* Define the specific buildTypes that this webhook should execute for.\n")
              .append(leftPad(2)).append(" * There are 3 ways to define a buildType.\n")
              .append(leftPad(2)).append(" *\n")
              .append(leftPad(2)).append(" *     This function takes an object of type\n")
              .append(leftPad(2)).append(" *     \"jetbrains.buildServer.configs.kotlin.BuildType\".\n")
              .append(leftPad(2)).append(" *     Typically, the buildType will already be defined in this file,\n")
              .append(leftPad(2)).append(" *     and we can just reference it.\n")
              .append(leftPad(2)).append(" * buildType(myBuildType)\n")
              .append(leftPad(2)).append(" *\n")
              .append(leftPad(2)).append(" *     This function takes the id of the BuildType. Again, we already\n")
              .append(leftPad(2)).append(" *     know the buildType config, so we can use that by calling\n")
              .append(leftPad(2)).append(" *     toString() on it.\n")
              .append(leftPad(2)).append(" * buildTypeId(myBuildType.id.toString())\n")
              .append(leftPad(2)).append(" *\n")
              .append(leftPad(2)).append(" *     This example calls the same function, but we are hard coding the\n")
              .append(leftPad(2)).append(" *     BuildType's ID string.\n")
              .append(leftPad(2)).append(" *     This is the least preferred method as it would need to be updated if\n")
              .append(leftPad(2)).append(" *     the ID changes.\n")
              .append(leftPad(2)).append(" * buildTypeId(\"MyProjectId_MyBuildTypeId\")\n")
              .append(leftPad(2)).append(" */\n")
              .append(leftPad(2)).append("\n");
            for (String bt : webHookConfig.getEnabledBuildTypesSet() ) {
                sb.append(leftPad(2)).append("buildTypeId{\"").append(this.buildTypeIdResolver.getExternalBuildTypeId(bt)).append("\"}\n");
            }
              sb.append(leftPad(1)).append("}\n");
        }
        return sb.toString();
    }

    private String getHeaders(WebHookConfig webHookConfig) {
        StringBuilder sb = new StringBuilder();
        if (!webHookConfig.getHeaders().isEmpty()) {
            sb.append(leftPad(1)).append("headers {\n");
            for (WebHookHeaderConfig h : webHookConfig.getHeaders()) {
                sb.append(leftPad(2)).append("header(\"").append(escapeKotlinString(h.getName())).append("\", \"").append(escapeKotlinString(h.getValue())).append("\")\n");
            }
            sb.append(leftPad(1)).append("}\n");
        }
        return sb.toString();
    }
    
    private String getParameters(WebHookConfig webHookConfig) {
        StringBuilder sb = new StringBuilder();
        if (!webHookConfig.getParams().isEmpty()) {
            sb.append(leftPad(1)).append("parameters {\n");
            for (WebHookParameterModel p : webHookConfig.getParams()) {
                if (isSimpleParameter(p)) {
                    sb.append(leftPad(2)).append("parameter(\"").append(escapeKotlinString(p.getName())).append("\", \"").append(escapeKotlinString(p.getValue())).append("\")\n");
                } else {
                    sb.append(leftPad(2)).append("parameter(\n")
                      .append(leftPad(3)).append("name = \"").append(escapeKotlinString(p.getName())).append("\",\n")
                      .append(leftPad(3)).append("value = \"").append(escapeKotlinString(p.getValue())).append("\"");
                    if (p.getSecure() != null ) {
                        sb.append(",\n").append(leftPad(3)).append("secure = ").append(p.getSecure());
                    }
                    if (p.getForceResolveTeamCityVariable() != null ) {
                        sb.append(",\n").append(leftPad(3)).append("forceResolveTeamCityVariable = ").append(p.getForceResolveTeamCityVariable());
                    }
                    if (p.getIncludedInLegacyPayloads() != null ) {
                        sb.append(",\n").append(leftPad(3)).append("includedInLegacyPayloads = ").append(p.getIncludedInLegacyPayloads());
                    }
                    if (p.getTemplateEngine() != null ) {
                        sb.append(",\n").append(leftPad(3)).append("templateEngine = \"").append(p.getTemplateEngine()).append("\"");
                    }
                    sb.append("\n").append(leftPad(2)).append(")\n");
                    
                }
            }
            sb.append(leftPad(1)).append("}\n");
        }
        return sb.toString();
    }
    
    /**
     * Checks that all the non-required fields are null.
     * If they are defined, we want to render a complex object in the DSL.
     * @param p
     * @return
     */
    private boolean isSimpleParameter(WebHookParameterModel p) {
        return     p.getSecure() == null 
                && p.getForceResolveTeamCityVariable() == null 
                && p.getIncludedInLegacyPayloads() == null 
                && p.getTemplateEngine() == null;
    }
    private String leftPad() {
        return StringUtils.leftPad("", leftPad);
    }
    private String leftPad(int indentMultiplier) {
        return StringUtils.leftPad("", leftPad + (indentMultiplier * INDENT_SIZE_SPACES));
    }
    
    /**
     * Converts strings like "${buildIsPersonal}" to "${'$'}{buildIsPersonal}"
     * since that appears to be how they should be formatted in settings.kts
     * @param stringToBeEscaped
     * @return
     */
    private String escapeKotlinString(String stringToBeEscaped) {
        if (stringToBeEscaped != null && !stringToBeEscaped.contains("${'$'}{") && stringToBeEscaped.contains("${")) {
            return stringToBeEscaped.replace("${", "${'$'}{");
        } else {
            return stringToBeEscaped;
        }
    }

}
