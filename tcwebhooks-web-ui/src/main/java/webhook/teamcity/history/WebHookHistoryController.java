package webhook.teamcity.history;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookHistoryController extends BaseController {
	
	private static final String ITEMS = "items";
	private static final String PARAM_NAME_VIEW = "view";
	private static final String MY_URL = "/webhooks/history.html";
	private String myPluginPath;
	private WebHookHistoryRepository myWebHookHistoryRepository;
	
	public WebHookHistoryController(
			@NotNull SBuildServer server,
			@NotNull PluginDescriptor pluginDescriptor, 
			@NotNull WebControllerManager webControllerManager,
			@NotNull WebHookHistoryRepository webHookHistoryRepository
		) 
	{
		super(server);
		this.myPluginPath = pluginDescriptor.getPluginResourcesPath();
		this.myWebHookHistoryRepository = webHookHistoryRepository;
		webControllerManager.registerController(MY_URL, this);
		
	}

	@Override
	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Map<String,Object> params = new HashMap<>(); 
		
		params.put("jspHome",this.myPluginPath);
    	params.put("includeJquery", Boolean.toString(this.myServer.getServerMajorVersion() < 7));
    	params.put("rootContext", myServer.getServerRootPath());
    	
    	params.put("errorCount", myWebHookHistoryRepository.getErroredCount());
    	params.put("okCount", myWebHookHistoryRepository.getOkCount());
    	params.put("skippedCount", myWebHookHistoryRepository.getDisabledCount());
    	params.put("totalCount", myWebHookHistoryRepository.getTotalCount());
		
		if (isGet(request) && request.getParameter(PARAM_NAME_VIEW) != null) {
			int pageNumber = getPageNumber(request);
			int pageSize = getPageSize(request);
			switch ( request.getParameter(PARAM_NAME_VIEW)) {
				case "errors":
					params.put(ITEMS, myWebHookHistoryRepository.findHistoryErroredItems(pageNumber, pageSize));
					break;
				case "skipped":
					params.put(ITEMS, myWebHookHistoryRepository.findHistoryDisabledItems(pageNumber, pageSize));
					break;
				case "ok":
					params.put(ITEMS, myWebHookHistoryRepository.findHistoryOkItems(pageNumber, pageSize));
					break;
				case "all":
				default:
					params.put(ITEMS, myWebHookHistoryRepository.findHistoryAllItems(pageNumber, pageSize));
					break;
			}
			
		}
		
		return new ModelAndView(myPluginPath + "WebHook/viewHistory.jsp", params); 
	}

	private int getPageSize(HttpServletRequest request) {
		if (request.getParameter("pageSize") != null) {
			return Integer.valueOf(request.getParameter("pageSize"));
		}
		return 50;
	}

	private int getPageNumber(HttpServletRequest request) {
		if (request.getParameter("pageNumber") != null) {
			return Integer.valueOf(request.getParameter("pageNumber"));
		}
		return 1;
	}

}
