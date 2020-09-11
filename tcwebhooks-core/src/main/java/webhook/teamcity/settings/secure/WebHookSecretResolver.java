package webhook.teamcity.settings.secure;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.SProject;

public interface WebHookSecretResolver {
	
	public String getSecret(@NotNull SProject sProject, @NotNull String token);

}
