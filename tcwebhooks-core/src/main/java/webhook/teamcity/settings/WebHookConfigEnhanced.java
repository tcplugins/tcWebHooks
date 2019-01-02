package webhook.teamcity.settings;

import java.util.Set;
import java.util.TreeSet;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.history.GeneralisedWebAddress;

/**
 * A class used internally by the {@link WebHookSettingsManager}
 * to store a cache of webhooks with some extra data added.<br>
 *
 * Is used so that searches and groupings by projectId and other data are quick.
 */

@Getter @Builder @ToString
public class WebHookConfigEnhanced {
	private WebHookConfig webHookConfig;
	@Builder.Default private Set<String> tags = new TreeSet<>();
	private String templateId;
	private String templateDescription;
	private String payloadFormat;
	private String payloadFormatDescription;
	private String projectExternalId;
	private Set<BuildStateEnum> buildStates;
	private GeneralisedWebAddress generalisedWebAddress;

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
							 .buildStates(new TreeSet<BuildStateEnum>(this.buildStates))
							 .generalisedWebAddress(
									 GeneralisedWebAddress.build(
											 this.generalisedWebAddress.getGeneralisedAddress(), 
											 this.generalisedWebAddress.getAddressType()
											 )
									 )
							 .build();
		if (this.tags != null && ! this.tags.isEmpty()) {
			w.tags = new TreeSet<>(this.tags);
		}

		return w;
	}

}
