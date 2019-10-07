package webhook.teamcity;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;

public class DeferrableServiceManagerImpl implements DeferrableServiceManager {
	
	List<DeferrableService> deferrableServices = new ArrayList<>();
	
	public DeferrableServiceManagerImpl(SBuildServer server) {
		server.addListener(new BuildServerAdapter() {
			@Override
			public void serverStartup() {
				deferrableServices.forEach(DeferrableService::register);
			}
		});
	}

	@Override
	public void registerService(DeferrableService deferrableService) {
		deferrableServices.add(deferrableService);
	}

}
