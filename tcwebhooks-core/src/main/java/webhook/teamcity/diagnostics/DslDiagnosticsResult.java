package webhook.teamcity.diagnostics;

import lombok.Data;

@Data
public class DslDiagnosticsResult {
	private String lastTeamCityDslUpdate;
	
	private boolean tcWebHooksDslFound;
	private String tcWebHooksDslVersion;
	private String lastTcWebHooksDslUpdate;
	private String lastTcWebHooksUpdateFriendly;
}
