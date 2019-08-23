package webhook.teamcity.extension.util;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplateResolver;

public class EnabledBuildStateResolver {

	private WebHookTemplateResolver templateResolver;
	private SProject project;

	public EnabledBuildStateResolver(WebHookTemplateResolver templateResolver, SProject project) {
		this.templateResolver = templateResolver;
		this.project = project;
	}

    public void checkAndAddBuildState(HttpServletRequest r, BuildState state, BuildStateEnum myBuildState, String varName){
    	if ((r.getParameter(varName) != null)
    		&& (r.getParameter(varName).equalsIgnoreCase("on"))
    		&& (templateResolver.templateSupportsFormatAndState(myBuildState, project, r.getParameter("payloadTemplate")))){
    		state.enable(myBuildState);
    	} else {
    		state.disable(myBuildState);
    	}
    }

    public void checkAndAddBuildStateIfEitherSet(HttpServletRequest r, BuildState state, BuildStateEnum myBuildState, String varName, String otherVarName){
    	if ((r.getParameter(varName) != null)
    			&& (r.getParameter(varName).equalsIgnoreCase("on"))){
    		state.enable(myBuildState);
    	} else if ((r.getParameter(otherVarName) != null)
    			&& (r.getParameter(otherVarName).equalsIgnoreCase("on"))){
	    	state.enable(myBuildState);
    	} else {
    		state.disable(myBuildState);
    	}
    }


}
