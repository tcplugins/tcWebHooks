package webhook.teamcity.settings.converter;

import lombok.AllArgsConstructor;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.auth.WebHookAuthenticationParameter;
import webhook.teamcity.auth.WebHookAuthenticatorFactory;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.settings.WebHookConfig;

@AllArgsConstructor
public class WebHookConfigToKotlinDslRenderer {
    private WebHookAuthenticatorProvider authenticatorProvider;
    private BuildTypeIdResolver buildTypeIdResolver;
    /*
      
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
      
     */
    
    public String renderAsKotlinDsl(WebHookConfig webHookConfig) {
        StringBuilder sb = new StringBuilder()
        .append("    webHookConfiguration {\n")
        .append("        webHookId = \"").append(webHookConfig.getUniqueKey()).append("\"\n")
        .append("        template = \"").append(webHookConfig.getPayloadTemplate()).append("\"\n")
        .append("        url = \"").append(webHookConfig.getUrl()).append("\"\n")
        .append(getBuildTypes(webHookConfig))
        .append(getBuildStates(webHookConfig))
        .append(getAuthentication(webHookConfig))
        .append("    }");
        return sb.toString();
    }

    private String getAuthentication(WebHookConfig webHookConfig) {
        StringBuilder sb = new StringBuilder();
        WebHookAuthConfig authenticationConfig = webHookConfig.getAuthenticationConfig();
        if (authenticationConfig != null) {
            WebHookAuthenticatorFactory factory = this.authenticatorProvider.findAuthenticatorFactoryByType(authenticationConfig.getType());
            sb.append("        authentication = ").append(factory.getKotlinDslName()).append(" {\n");
            for (WebHookAuthenticationParameter param : factory.getParameterList()) {
                if (authenticationConfig.getParameters().containsKey(param.getKey())) {
                    sb.append("            ").append(param.getKey()).append(" = \"").append(authenticationConfig.getParameters().get(param.getKey())).append("\"\n");
                }
            }
            sb.append("            preemptive = ").append(authenticationConfig.getPreemptive().toString()).append("\n")
            .append("        }\n");
        }
        return sb.toString();
    }

    private String getBuildStates(WebHookConfig webHookConfig) {
        StringBuilder sb = new StringBuilder();
        sb.append("        buildStates {\n");
        for (BuildStateEnum state : BuildStateEnum.values()) {
            if (webHookConfig.isEnabledForBuildState(state)) {
                sb.append("            ").append(state.getShortName()).append(" = true\n");
            }
        }
        sb.append("        }\n");
        return sb.toString();
    }

    private String getBuildTypes(WebHookConfig webHookConfig) {
        StringBuilder sb = new StringBuilder();
        if (webHookConfig.isEnabledForAllBuildsInProject()) {
            sb.append("        allProjectBuilds {\n")
              .append("            subProjectBuilds = ").append(Boolean.toString(webHookConfig.isEnabledForSubProjects())).append("\n")
              .append("        }\n");
        } else {
            sb.append("        buildTypes = selectedProjectBuilds {\n")
              .append("            subProjectBuilds = ").append(Boolean.toString(webHookConfig.isEnabledForSubProjects())).append("\n")
              .append("            /* Define the specific buildTypes that this webhook should execute for.\n")
              .append("             * There are 3 ways to define a buildType.\n")
              .append("             *\n")
              .append("             *     This function takes an object of type \"jetbrains. buildServer. configs. kotlin.BuildType\".\n")
              .append("             *     Typically, the buildType will already be defined in this file, and we can just reference it.\n")
              .append("             * buildType(myBuildType)\n")
              .append("             *\n")
              .append("             *     This function takes the id of the BuildType. Again, we already know the buildType config, so\n")
              .append("             *     we can use that by calling toString() on it.\n")
              .append("             * buildTypeId(myBuildType.id.toString())\n")
              .append("             *\n")
              .append("             *     This example calls the same function, but we are hard coding the BuildType's ID string.\n")
              .append("             *     This is the least preferred method as it would need to be updated if the ID changes.\n")
              .append("             * buildTypeId(\"MyProjectId_MyBuildTypeId\")\n")
              .append("             */\n")
              .append("            \n");
            for (String bt : webHookConfig.getEnabledBuildTypesSet() ) {
                sb.append("            buildTypeId{\"").append(this.buildTypeIdResolver.getExternalBuildTypeId(bt)).append("\"}\n");
            }
              sb.append("        }\n");
        }
        return sb.toString();
    }

}
