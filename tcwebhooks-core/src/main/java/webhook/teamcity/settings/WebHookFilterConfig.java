package webhook.teamcity.settings;

import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.jdom.Element;

@Getter @Setter @NoArgsConstructor
public class WebHookFilterConfig {
	
	public static final String XML_ELEMENT_NAME = "filter";
	public static final String XML_ATTR_VALUE = "value";
	public static final String XML_ATTR_REGEX = "regex";
	public static final String XML_ATTR_ENABLED = "enabled";
	
	
	String value;
	String regex;
	boolean enabled;
	Pattern pattern; 
	
	
	public static WebHookFilterConfig create(String value, String regex, boolean enabled){
		WebHookFilterConfig t = new WebHookFilterConfig();
		t.value = value;
		t.regex = regex;
		t.enabled = enabled;
		t.pattern = Pattern.compile(t.regex);  
		return t;
	}
	
	public static WebHookFilterConfig copy(WebHookFilterConfig config){
		WebHookFilterConfig t = new WebHookFilterConfig();
		t.value = config.value;
		t.regex = config.regex;
		t.enabled = config.enabled;
		t.pattern = Pattern.compile(t.regex);  
		return t;
	}

	public Element getAsElement() {
		Element e = new Element(XML_ELEMENT_NAME);
			e.setAttribute(XML_ATTR_VALUE, this.value);
			e.setAttribute(XML_ATTR_REGEX, this.regex);
			e.setAttribute(XML_ATTR_ENABLED, String.valueOf(this.enabled));
		return e;
	}

}
