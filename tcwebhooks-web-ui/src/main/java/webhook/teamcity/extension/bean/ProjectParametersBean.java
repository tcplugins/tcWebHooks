package webhook.teamcity.extension.bean;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.serverSide.SProject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import webhook.teamcity.settings.project.WebHookParameter;

@Getter @AllArgsConstructor
public class ProjectParametersBean {
	
	SProject project;
	List<WebHookParameter> parameterList;
	
	public static ProjectParametersBean newInstance(SProject project, List<WebHookParameter> parameters) {
		if (parameters != null) {
			return new ProjectParametersBean(project, parameters);
		} else {
			return new ProjectParametersBean(project, new ArrayList<>());
		}
	}

}
