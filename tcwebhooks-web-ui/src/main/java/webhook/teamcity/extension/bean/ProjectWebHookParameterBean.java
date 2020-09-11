package webhook.teamcity.extension.bean;

import jetbrains.buildServer.serverSide.SProject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import webhook.CommonUtils;
import webhook.teamcity.settings.project.WebHookParameter;

@Getter
@AllArgsConstructor
public class ProjectWebHookParameterBean {
	SProject sproject;
	WebHookParameter parameter;
	
	public String getSensibleProjectFullName() {
		return CommonUtils.getSensibleProjectFullName(getSproject());
	}
	
	public String getSensibleProjectName() {
		return CommonUtils.getSensibleProjectName(getSproject());
	}
}
