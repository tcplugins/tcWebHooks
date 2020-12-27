package webhook.teamcity.server.rest.jersey;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jetbrains.annotations.NotNull;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import webhook.teamcity.server.rest.data.WebHookConfigurationValidator;
import webhook.teamcity.server.rest.data.WebHookParameterValidator;

@Provider
public class WebHookConfigurationValidatorProvider implements InjectableProvider<Context, Type>, Injectable<WebHookConfigurationValidator> {
	private final WebHookConfigurationValidator webhookValidator;

	public WebHookConfigurationValidatorProvider(@NotNull final PermissionChecker permissionChecker, @NotNull final ProjectManager projectManager) {
		this.webhookValidator = new WebHookConfigurationValidator(permissionChecker, projectManager);
	}

	public ComponentScope getScope() {
		return ComponentScope.Singleton;
	}

	public Injectable<WebHookConfigurationValidator> getInjectable(final ComponentContext ic, final Context context, final Type type) {
		if (type.equals(WebHookConfigurationValidator.class)) {
			return this;
		}
		return null;
	}

	public WebHookConfigurationValidator getValue() {
		return webhookValidator;
	}

}