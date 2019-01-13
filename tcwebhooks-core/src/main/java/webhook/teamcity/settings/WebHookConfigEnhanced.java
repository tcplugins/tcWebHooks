package webhook.teamcity.settings;

import java.util.Set;
import java.util.TreeSet;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import webhook.teamcity.BuildStateEnum;

@Getter @Builder @ToString
public class WebHookConfigEnhanced {
	private WebHookConfig webHookConfig;
	@Builder.Default private Set<String> tags = new TreeSet<>();
	private String templateId;
	private String templateDescription;
	private String payloadFormat;
	private String payloadFormatDescription;
	private String projectExternalId;
	private Set<String> buildTypeExternalIds;
	private Set<BuildStateEnum> buildStates;
	
	public WebHookConfigEnhanced addTag(String tag) {
		if (! tag.isEmpty() ) {
			this.tags.add(tag);
		}
		return this;
	}
	
	public WebHookConfigEnhanced copy() {
		WebHookConfigEnhanced w = WebHookConfigEnhanced
							 .builder()
							 .webHookConfig(this.webHookConfig.copy())
							 .templateId(this.templateId)
							 .templateDescription(this.templateDescription)
							 .payloadFormat(this.payloadFormat)
							 .payloadFormatDescription(this.payloadFormatDescription)
							 .projectExternalId(this.projectExternalId)
							 .build();
		if (this.tags != null && ! this.tags.isEmpty()) {
			w.tags = new TreeSet<String>(this.tags);
		}
							 
		return w;
	}
	
}