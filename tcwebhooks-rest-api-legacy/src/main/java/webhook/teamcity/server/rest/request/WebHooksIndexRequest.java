package webhook.teamcity.server.rest.request;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import webhook.teamcity.WebHookPluginDataResolver;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.util.BeanContext;

@Path(Constants.API_URL)
public class WebHooksIndexRequest {
	
	@Context
	@NotNull
	private BeanContext myBeanContext;
	@Context
	@NotNull
	private WebHookPluginDataResolver myWebHookPluginDataResolver;

	@GET
	@Produces({"application/xml", "application/json"})
	public WebHooksIndexItems index() {
		return new WebHooksIndexItems(
				myWebHookPluginDataResolver.getWebHooksCoreVersion(), 
				myWebHookPluginDataResolver.getWebHooksRestApiVersion(), 
				myBeanContext.getApiUrlBuilder()
			); 
	}
	
	@XmlRootElement
	@AllArgsConstructor @NoArgsConstructor @Getter
	public static class WebHooksIndexItem {
		@XmlAttribute
		private String href;
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	@Getter @AllArgsConstructor @NoArgsConstructor
	public static class WebHooksIndexItems {
		@XmlAttribute
		private String tcWebHooksVersion; 
		@XmlAttribute
		private String tcWebHooksRestApiVersion; 
		
		@XmlElement
		private WebHooksIndexItem configurations;
		@XmlElement
		private WebHooksIndexItem parameters;
		@XmlElement
		private WebHooksIndexItem templates;

		public WebHooksIndexItems(String coreVersion, String restVersion, WebHookApiUrlBuilder apiUrlBuilder) {
			tcWebHooksVersion = coreVersion;
			tcWebHooksRestApiVersion = restVersion;
			configurations = new WebHooksIndexItem(apiUrlBuilder.getConfigurationsHref());
			parameters = new WebHooksIndexItem(apiUrlBuilder.getParametersHref()); 
			templates = new WebHooksIndexItem(apiUrlBuilder.getTemplatesHref()); 
		}
	}
}
