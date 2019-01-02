package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import webhook.teamcity.extension.bean.ProjectWebHooksBean;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookConfigEnhanced;
import webhook.teamcity.settings.WebHookSearchFilter;
import webhook.teamcity.settings.WebHookSearchResult;
import webhook.teamcity.settings.WebHookSettingsManager;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookSearchController extends BaseController {

	private PluginDescriptor myPluginDescriptor;
	private WebHookSettingsManager myWebHookSettingsManager;
	private ProjectManager myProjectManager;
	private WebAddressTransformer myWebAddressTransformer;
	private WebHookPayloadManager myWebHookPayloadManager;
	private WebHookTemplateResolver myWebHookTemplateResolver;

	public WebHookSearchController(SBuildServer server, WebControllerManager webControllerManager,
    		PluginDescriptor pluginDescriptor, WebHookSettingsManager webHookSettingsManager,
    		ProjectManager projectManager, WebAddressTransformer webAddressTransformer,
    		@NotNull WebHookPayloadManager payloadManager,
			@NotNull WebHookTemplateResolver templateResolver) {
		super(server);
		myPluginDescriptor = pluginDescriptor;
		myWebHookSettingsManager = webHookSettingsManager;
		myProjectManager = projectManager;
		myWebAddressTransformer = webAddressTransformer;
		myWebHookPayloadManager = payloadManager;
		myWebHookTemplateResolver = templateResolver;
		webControllerManager.registerController("/webhooks/search.html", this);
    }

    @Nullable
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HashMap<String,Object> params = new HashMap<>();
        List<ProjectWebHooksBean> searchResults = new ArrayList<>();
        int resultCount = 0;
        int allResultCount = 0;
        boolean resultsRestricted = false;
        for (Map.Entry<String, List<WebHookSearchResult>> entry : myWebHookSettingsManager.findWebHooksByProject(buildFilter(request)).entrySet()) {
        	allResultCount += entry.getValue().size();
        	try {
	        	SProject project = myProjectManager.findProjectById(entry.getKey());
	        	ProjectWebHooksBean result =
	        			ProjectWebHooksBean.buildWithoutNew(getListOfWebHookConfigs(entry.getValue()), project,
	        					myWebHookPayloadManager.getRegisteredFormatsAsCollection(),
	        					myWebHookTemplateResolver.findWebHookTemplatesForProject(project),
	        					myWebHookSettingsManager.iswebHooksEnabledForProject(project.getProjectId()));
	        	searchResults.add(result);
	        	resultCount += result.getWebHookList().size();
        	} catch (AccessDeniedException ex) {
        		resultsRestricted = true;
        	}
        }
        params.put("jspHome", myPluginDescriptor.getPluginResourcesPath());
        params.put("searchResults", searchResults);
        params.put("resultCount", resultCount);
        params.put("allResultCount", allResultCount);
		params.put("payloadFormats", myWebHookPayloadManager.getTemplatedFormats());
		params.put("resultsRestricted", resultsRestricted);

        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/webHookSearch.jsp", params);
    }

    private List<WebHookConfigEnhanced> getListOfWebHookConfigs(List<WebHookSearchResult> searchResults) {
    	List<WebHookConfigEnhanced> configs = new ArrayList<>();
    	for (WebHookSearchResult result : searchResults) {
    		configs.add(result.getWebHookConfigEnhanced());
    	}
		return configs;
	}

	private WebHookSearchFilter buildFilter(HttpServletRequest request) {
    	WebHookSearchFilter filter = WebHookSearchFilter.builder()
    			.show(request.getParameter("show"))
    			.formatShortName(request.getParameter("format"))
    			.templateId(request.getParameter("templateId"))
    			.textSearch(request.getParameter("search"))
    			.urlSubString(request.getParameter("url"))
    			.webhookId(request.getParameter("webhookId"))
				.projectExternalId(request.getParameter("projectId"))
				.buildTypeExternalId(request.getParameter("buildTypeId"))
    		.build();

    	for (String tag: getTags(request)) {
    		filter.addTag(tag);
    	}

    	return filter;
    }

	private Set<String> getTags(HttpServletRequest request) {
		if (request.getParameterValues("tag") != null) {
			return new HashSet<>(Arrays.asList(request.getParameterValues("tag")));
		}
		return new HashSet<>();
	}

}
