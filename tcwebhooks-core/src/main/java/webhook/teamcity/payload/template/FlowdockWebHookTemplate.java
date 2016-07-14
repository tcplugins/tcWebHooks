package webhook.teamcity.payload.template;

import java.util.Map;
import java.util.TreeMap;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;

public class FlowdockWebHookTemplate extends AbstractFileSetBasedWebHookTemplate {

	private Map<BuildStateEnum, String> normalTemplateMap = new TreeMap<>();
	private Map<BuildStateEnum, String> branchTemplateMap = new TreeMap<>();

	public FlowdockWebHookTemplate(WebHookTemplateManager manager) {
		super(manager);
		
		normalTemplateMap.put(BuildStateEnum.BUILD_BROKEN, "flowdock-buildBroken-buildFailed-buildInterrupted-normal.json");
		normalTemplateMap.put(BuildStateEnum.BUILD_FAILED, "flowdock-buildBroken-buildFailed-buildInterrupted-normal.json");
		normalTemplateMap.put(BuildStateEnum.BUILD_INTERRUPTED, "flowdock-buildBroken-buildFailed-buildInterrupted-normal.json");
		normalTemplateMap.put(BuildStateEnum.BUILD_STARTED, "flowdock-buildStarted-normal.json");
		normalTemplateMap.put(BuildStateEnum.BUILD_SUCCESSFUL, "flowdock-buildSuccessful-buildFixed-normal.json");
		normalTemplateMap.put(BuildStateEnum.BUILD_FIXED, "flowdock-buildSuccessful-buildFixed-normal.json");
		normalTemplateMap.put(BuildStateEnum.RESPONSIBILITY_CHANGED, "flowdock-responsibilityChanged-normal.json");
		
		branchTemplateMap.put(BuildStateEnum.BUILD_BROKEN, "flowdock-buildBroken-buildFailed-buildInterrupted-branch.json");
		branchTemplateMap.put(BuildStateEnum.BUILD_FAILED, "flowdock-buildBroken-buildFailed-buildInterrupted-branch.json");
		branchTemplateMap.put(BuildStateEnum.BUILD_INTERRUPTED, "flowdock-buildBroken-buildFailed-buildInterrupted-branch.json");
		branchTemplateMap.put(BuildStateEnum.BUILD_STARTED, "flowdock-buildStarted-branch.json");
		branchTemplateMap.put(BuildStateEnum.BUILD_SUCCESSFUL, "flowdock-buildSuccessful-buildFixed-branch.json");
		branchTemplateMap.put(BuildStateEnum.BUILD_FIXED, "flowdock-buildSuccessful-buildFixed-branch.json");
		branchTemplateMap.put(BuildStateEnum.RESPONSIBILITY_CHANGED, "flowdock-responsibilityChanged-branch.json");
		
	}

	@Override
	public String getTemplateDescription() {
		return "Flowdock JSON templates";
	}

	@Override
	public String getTemplateToolTipText() {
		return "Supports the TeamCity Flowdock JSON integration";
	}

	@Override
	public String getTemplateShortName() {
		return "flowdock";
	}

	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return payloadFormat.equalsIgnoreCase(WebHookPayloadJsonTemplate.FORMAT_SHORT_NAME);
	}

	@Override
	public String getPreferredDateTimeFormat() {
		return "";
	}

	@Override
	public String getLoggingName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTemplateFilesLocation() {
		return "webhook/teamcity/payload/template/flowdock/";
	}

	@Override
	public Map<BuildStateEnum, String> getNormalTemplateMap() {
		return this.normalTemplateMap;
	}
	
	@Override
	public Map<BuildStateEnum, String> getBranchTemplateMap() {
		return this.branchTemplateMap;
	}

}
