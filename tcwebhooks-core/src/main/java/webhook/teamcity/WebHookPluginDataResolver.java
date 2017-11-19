package webhook.teamcity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WebHookPluginDataResolver {
	
	boolean isWebHooksRestApiInstalled();
	@Nullable String getWebHooksRestApiVersion();
	boolean isWebHooksCoreAndApiVersionTheSame();
	@NotNull String getWebHooksCoreVersion();

}
