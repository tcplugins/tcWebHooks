package webhook.teamcity.settings.converter;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableMap;

import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;

/**
 * A helper class to allow rolling back from 1.2 to 1.1 format in plugin-settings.xml.
 * In 1.2, legacy formats are represented by template ids, so rolling back requires
 * converting a template id into its old name and format.
 *
 */
public class TemplateToPayloadRollbackConverter {
	
	private TemplateToPayloadRollbackConverter() {}
	
	public static final String NO_TEMPLATE_ID = "none";
	
	public static final String XML_TEMPLATE_ID = "legacy-xml";
	public static final String XML_FORMAT_SHORT_NAME = "xml";

	public static final String EMPTY_TEMPLATE_ID = "legacy-empty";
	public static final String EMPTY_FORMAT_SHORT_NAME = "empty";
	
	public static final String NVPAIRS_TEMPLATE_ID = "legacy-nvpairs";
	public static final String NVPAIRS_FORMAT_SHORT_NAME = "nvpairs";

	public static final String JSON_TEMPLATE_ID = "legacy-json";
	public static final String JSON_FORMAT_SHORT_NAME = "json";

	public static final String TAILORED_JSON_TEMPLATE_ID = "legacy-tailored-json";
	public static final String TAILORED_JSON_FORMAT_SHORT_NAME = "tailoredjson";
	
	static final Map<String, String> MAP_OF_TEMPLATES = ImmutableMap.of(
			XML_TEMPLATE_ID, XML_FORMAT_SHORT_NAME,
			EMPTY_TEMPLATE_ID, EMPTY_FORMAT_SHORT_NAME,
			NVPAIRS_TEMPLATE_ID, NVPAIRS_FORMAT_SHORT_NAME,
			JSON_TEMPLATE_ID, JSON_FORMAT_SHORT_NAME,
			TAILORED_JSON_TEMPLATE_ID, TAILORED_JSON_FORMAT_SHORT_NAME
		);
	
	static final Collection<String> FORMATS_FROM_1_1 = MAP_OF_TEMPLATES.values();

	public static Pair<String,String> transformTemplateToPayloadAndTemplate(String formatId, String templateId) {
		
		// First check if it's a new format. Need to do this first, since a null format will
		// default to NVpairs in 1.1.
		if (MAP_OF_TEMPLATES.containsKey(templateId)) {
			return Pair.of(MAP_OF_TEMPLATES.get(templateId), NO_TEMPLATE_ID);
		}
		
		// Next check if it's a valid 1.1 template setup correctly.
		if (templateId != null 
				&& !templateId.equalsIgnoreCase("none") 
				&& !templateId.isEmpty()
				&& WebHookPayloadJsonTemplate.FORMAT_SHORT_NAME.equalsIgnoreCase(formatId)) {
			
			return Pair.of(formatId, templateId);
		}
		
		// Next check if it has a valid template from 1.2 and map it to jsonTemplate.
		// By default it will have been nvpairs, which is incorrect 
		if (templateId != null 
				&& !templateId.equalsIgnoreCase("none") 
				&& !templateId.isEmpty()
				&& !MAP_OF_TEMPLATES.containsKey(templateId.toLowerCase())) {
			
			return Pair.of(WebHookPayloadJsonTemplate.FORMAT_SHORT_NAME, templateId);
		}
		
		// If we get here, we have a null template, and a valid 1.1 format
		// Just return it so that we don't modify existing webhooks for no reason (eg, set template to none)
		return Pair.of(formatId, templateId); 
		
	}
}
