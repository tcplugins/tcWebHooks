package webhook.teamcity.settings;

import java.util.Set;
import java.util.TreeSet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
	@Builder.Default private Set<Tag> tags = new TreeSet<>();
	private String templateId;
	private String templateDescription;
	private String payloadFormat;
	private String payloadFormatDescription;
	private String projectExternalId;
	private String projectInternalId;
	private Set<BuildStateEnum> buildStates;
	private GeneralisedWebAddress generalisedWebAddress;

	public WebHookConfigEnhanced addTag(String tagText, TagType tagType) {
	    addTag(new Tag(tagText, tagType));
	    return this;
	}
	public WebHookConfigEnhanced addTag(Tag tag) {
		if (! tag.getName().isEmpty() ) {
			this.tags.add(tag);
		}
		return this;
	}
	
	public Set<String> getTagNames() {
	    Set<String> tagNames = new TreeSet<>();
	    for (Tag tagName : tags) {
	        tagNames.add(tagName.getName());
            
        }
	    return tagNames;
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
							 .projectInternalId(this.projectInternalId)
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
	
	@Data @AllArgsConstructor
	public static class Tag implements Comparable<Tag> {
	    private String name;
	    private TagType type;
	    
        @Override
        public int compareTo(Tag t) {
            return t.getName().compareTo(name);
        }
	}
	
	public enum TagType {
	    USER_DEFINED_TAG ("User defined tag"),
	    GENERALISED_URL ("A shortened version of the URL configured for this webhook"),
	    TEMPLATE_ID ("Template ID for the template associated with this webhook"),
	    HEADER ("This webhook has one or more headers configured"),
	    FILTER ("This webhook has one or more filters configured"),
	    PARAMETER ("This webhook has one or more 'Webhook Parameters' configured. "
	            + "Note: these are not the same as 'WebHook Project Parameters' which are defined against "
	            + "a project and are inherited by all webhooks in the project and sub-projects"),
	    FORMAT ("The format of the webhook payload. Determined by the associated template"), 
	    WEBHOOK_ENABLED ("Whether the webhook is enabled or disabled. Disabled webhooks will be skipped"), 
	    SHOW_SECURE ("Whether secure values are shown or hidden in the UI and logged to the teamcity-server.log file"), 
	    AUTHENTICATED("This webhook has authentication configured"), 
	    AUTHENTICATION_TYPE("The type of authentication configured for this webhook");

        private String description;

        TagType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
	}

}
