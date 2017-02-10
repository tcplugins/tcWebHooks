package webhook.teamcity.settings;

import lombok.Getter;
import org.jdom.Element;

import java.util.regex.Pattern;

@Getter
public class WebHookFilterConfig {
	
	public static final String XML_ELEMENT_NAME = "filter";
	public static final String VALUE = "value";
	public static final String REGEX = "regex";
	public static final String ENABLED = "enabled";
	
	
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
			e.setAttribute(VALUE, this.value);
			e.setAttribute(REGEX, this.regex);
			e.setAttribute(ENABLED, String.valueOf(this.enabled));
		return e;
	}

	public Boolean isEnabled() {
		return this.enabled;
	}

	public Pattern getPattern() {
		return this.pattern;
	}

	public String getValue() {
		return this.value;
	}

	public String getRegex() {
		return this.regex;
	}
}
