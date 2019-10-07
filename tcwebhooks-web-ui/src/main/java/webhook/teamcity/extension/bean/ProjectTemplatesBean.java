package webhook.teamcity.extension.bean;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.serverSide.SProject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import webhook.teamcity.payload.WebHookPayloadTemplate;

@Getter @AllArgsConstructor
public class ProjectTemplatesBean {
	
	SProject project;
	List<WebHookPayloadTemplate> templateList;
	
	public static ProjectTemplatesBean newInstance(SProject project, List<WebHookPayloadTemplate> templates) {
		if (templates != null) {
			return new ProjectTemplatesBean(project, templates);
		} else {
			return new ProjectTemplatesBean(project, new ArrayList<>());
		}
	}

}
