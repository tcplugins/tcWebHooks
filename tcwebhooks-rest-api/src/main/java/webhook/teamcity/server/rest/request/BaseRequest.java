package webhook.teamcity.server.rest.request;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Context;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.server.rest.ApiUrlBuilder;
import jetbrains.buildServer.server.rest.PathTransformer;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.WebHookWebLinks;
import webhook.teamcity.server.rest.util.WebHookBeanContext;

public class BaseRequest {
	
	private @Context @NotNull ApiUrlBuilder apiUrlBuilder;
	private @Context @NotNull WebHookWebLinks webHookWebLinks;
	protected WebHookBeanContext myWebHookBeanContext;
	protected WebHookApiUrlBuilder myWebHookApiUrlBuilder;
	
	@PostConstruct
	public void postConstruct() throws IllegalArgumentException, IllegalAccessException {
		this.myWebHookApiUrlBuilder = new WebHookApiUrlBuilder(getPathTransformer(this.apiUrlBuilder));
		this.myWebHookBeanContext = new WebHookBeanContext(this.webHookWebLinks, this.myWebHookApiUrlBuilder);
	}
	
	@SuppressWarnings("java:S3011")
	private PathTransformer getPathTransformer(ApiUrlBuilder apiUrlBuilder) throws IllegalArgumentException, IllegalAccessException {
		List<Field> allFields = Arrays.asList(ApiUrlBuilder.class.getDeclaredFields());

		Field myPathTransformer = allFields.stream()
		    .filter(field -> field.getName().equals("myPathTransformer"))
		    .findFirst().orElseThrow(() -> new RuntimeException("Field not found"));
		myPathTransformer.setAccessible(true);
		return (PathTransformer) myPathTransformer.get(apiUrlBuilder);
	}

}
