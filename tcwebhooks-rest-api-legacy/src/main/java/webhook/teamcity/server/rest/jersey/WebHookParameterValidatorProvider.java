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
import webhook.teamcity.server.rest.data.WebHookParameterValidator;
import webhook.teamcity.settings.project.WebHookParameterStoreFactory;

@Provider
public class WebHookParameterValidatorProvider implements InjectableProvider<Context, Type>, Injectable<WebHookParameterValidator> {
	private final WebHookParameterValidator parameterValidator;

	public WebHookParameterValidatorProvider(@NotNull final WebHookParameterStoreFactory webHookParameterStoreFactory,
			@NotNull final PermissionChecker permissionChecker, @NotNull final ProjectManager projectManager) {
		this.parameterValidator = new WebHookParameterValidator(webHookParameterStoreFactory.getWebHookParameterStore(), permissionChecker, projectManager);
	}

	public ComponentScope getScope() {
		return ComponentScope.Singleton;
	}

	public Injectable<WebHookParameterValidator> getInjectable(final ComponentContext ic, final Context context, final Type type) {
		if (type.equals(WebHookParameterValidator.class)) {
			return this;
		}
		return null;
	}

	public WebHookParameterValidator getValue() {
		return parameterValidator;
	}

}