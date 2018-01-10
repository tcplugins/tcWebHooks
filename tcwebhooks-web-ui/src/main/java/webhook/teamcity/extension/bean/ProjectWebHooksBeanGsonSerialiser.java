package webhook.teamcity.extension.bean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import webhook.teamcity.extension.bean.TemplatesAndProjectWebHooksBean.TemplatesAndProjectWebHooksBeanResponseWrapper;
import webhook.teamcity.extension.util.ProjectHistoryResolver.ProjectHistoryBean;

public class ProjectWebHooksBeanGsonSerialiser {
    private ProjectWebHooksBeanGsonSerialiser(){}
	
	public static String serialise(TemplatesAndProjectWebHooksBeanResponseWrapper project){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(project);
	}
	public static String serialise(ProjectHistoryBean project){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(project);
	}

}
