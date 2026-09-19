package webhook.teamcity.extension;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class WebHookDokkaDocsController extends BaseController {

	private final PluginDescriptor myPluginDescriptor;

	public WebHookDokkaDocsController(@NotNull WebControllerManager manager, @NotNull PluginDescriptor descriptor) {
		this.myPluginDescriptor = descriptor;
		manager.registerController("/app/webhooks/kotlin-dsl/**", this);
	}

	@Nullable
	@Override
	protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response)
			throws Exception {

		// 1. Extract the requested relative file path out of the URL string
		String requestUri = request.getRequestURI();
		String contextPath = request.getContextPath();
		String relativePath = requestUri.substring((contextPath + "/app/webhooks/kotlin-dsl/").length());

		// Default to index.html if looking at the root of the route
		if (relativePath.isEmpty() || relativePath.endsWith("/")) {
			relativePath += "index.html";
		}

		// 2. Resolve TeamCity's runtime path to the extracted web resources
		// getPluginResourcesPath() returns "/plugins/tcWebHooks/"
		String resourcesWebPath = myPluginDescriptor.getPluginResourcesPath("dokka/" + relativePath);

		String systemPath = request.getSession().getServletContext().getRealPath(resourcesWebPath);

		if (systemPath == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not map path.");
			return null;
		}

		File requestedFile = new File(systemPath);

		// 3. Resolve the content type dynamically (crucial for CSS/JS/HTML mapping)
		String contentType = Files.probeContentType(requestedFile.toPath());
		if (contentType != null) {
			response.setContentType(contentType);
		} else if (requestedFile.getName().endsWith(".html")) {
			response.setContentType("text/html;charset=UTF-8");
		}

		// 4. Stream the file content back directly to the response
		try (FileInputStream in = new FileInputStream(requestedFile); OutputStream out = response.getOutputStream()) {
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		}

		// Returning null tells Spring that the response has been handled manually
		return null;
	}
}
