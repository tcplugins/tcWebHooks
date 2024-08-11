package webhook.teamcity;

import com.intellij.openapi.diagnostic.Logger;

public final class Loggers {
	public static final Logger SERVER 		= Logger.getInstance("jetbrains.buildServer.SERVER.tcWebHooks");
	
	private Loggers(){}
}
