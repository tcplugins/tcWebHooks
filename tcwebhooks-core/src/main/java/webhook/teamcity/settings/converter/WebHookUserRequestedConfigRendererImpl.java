package webhook.teamcity.settings.converter;

import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.serverSide.SProject;
import lombok.RequiredArgsConstructor;
import webhook.teamcity.TeamCityCoreFacade;
import webhook.teamcity.TeamCityCoreFacade.ProjectVcsStatus;
import webhook.teamcity.payload.template.render.HtmlRenderer;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;
import webhook.teamcity.payload.template.render.XmlToHtmlPrettyPrintingRenderer;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.testing.model.WebHookRenderResult;

@RequiredArgsConstructor
public class WebHookUserRequestedConfigRendererImpl implements WebHookUserRequestedConfigRenderer {
    private final WebHookConfigToKotlinDslRenderer myConfigToKotlinDslRenderer;
    private final WebHookConfigToProjectFeatureXmlRenderer myConfigToProjectFeatureXmlRenderer;
    private final TeamCityCoreFacade myTeamCityCoreFacade;
    

    @Override
    public WebHookRenderResult requestWebHookConfigurationAsCode(WebHookConfig webHookConfig) {
        try {
            @Nullable
            SProject project = myTeamCityCoreFacade.findProjectByIntId(webHookConfig.getProjectInternalId());
            @NotNull
            ProjectVcsStatus vcsStatus = myTeamCityCoreFacade.getProjectVcsStatus(project);
            if (vcsStatus.isKotlin()) {
                HtmlRenderer htmlr = new HtmlRenderer();
                return new WebHookRenderResult(htmlr.render(myConfigToKotlinDslRenderer.renderAsKotlinDsl(webHookConfig)), "javascript");
            } else {
                WebHookStringRenderer htmlr = new XmlToHtmlPrettyPrintingRenderer();
                return new WebHookRenderResult(htmlr.render(myConfigToProjectFeatureXmlRenderer.renderAsXml(Collections.singletonList(webHookConfig), project)), "xml");
            }
        } catch (Exception e) {
            return new WebHookRenderResult(e.getMessage(), e);
        }
    }

}
