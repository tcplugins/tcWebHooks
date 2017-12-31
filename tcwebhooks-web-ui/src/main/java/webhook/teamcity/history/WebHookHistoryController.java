package webhook.teamcity.history;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.util.Pager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookHistoryController extends BaseController {
	
	private static final String PARAM_NAME_COUNT_CONTEXT = "countContext";
	private static final String PARAM_NAME_ITEMS = "items";
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
			params.put("page", pageNumber);
			PagedList<WebHookHistoryItem> pagedList;
			switch ( request.getParameter(PARAM_NAME_VIEW)) {
				case "errors":
				case "Errors":
					pagedList = myWebHookHistoryRepository.findHistoryErroredItems(pageNumber, pageSize);
					params.put(PARAM_NAME_COUNT_CONTEXT, "Errored");
					break;
				case "skipped":
				case "Skipped":
					pagedList = myWebHookHistoryRepository.findHistoryDisabledItems(pageNumber, pageSize);
					params.put(PARAM_NAME_COUNT_CONTEXT, "Skipped");
					break;
				case "ok":
				case "Ok":
					pagedList = myWebHookHistoryRepository.findHistoryOkItems(pageNumber, pageSize);
					params.put(PARAM_NAME_COUNT_CONTEXT, "OK");
					break;
				case "all":
				case "All":
				default:
					pagedList = myWebHookHistoryRepository.findHistoryAllItems(pageNumber, pageSize);
					params.put(PARAM_NAME_COUNT_CONTEXT, "All");
					break;
			}
			
			params.put(PARAM_NAME_ITEMS, pagedList);
			
			Pager pager = new Pager(50);
			pager.setNumberOfRecords(pagedList.getTotalItems());
			pager.setRecordsPerPage(pageSize);
			pager.setCurrentPage(pageNumber);
			params.put("historyPager", pager);
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
		if (request.getParameter("page") != null) {
			return Integer.valueOf(request.getParameter("page"));
		}
		return 1;
	}

}
