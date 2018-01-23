package webhook.teamcity.settings;

import java.util.regex.Pattern;

import lombok.Getter;

import org.jdom.Element;

@Getter
public class WebHookHeaderConfig {
	
	public static final String XML_ELEMENT_NAME = "header";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	
	
	String name;
	String value;
	
	
	public static WebHookHeaderConfig create(String name, String value){
		WebHookHeaderConfig t = new WebHookHeaderConfig();
		t.name = name;
		t.value = value;
		return t;
	}
	
	public static WebHookHeaderConfig copy(WebHookHeaderConfig config){
		WebHookHeaderConfig t = new WebHookHeaderConfig();
		t.name = config.name;
		t.value = config.value;
		return t;
	}

	public Element getAsElement() {
		Element e = new Element(XML_ELEMENT_NAME);
			e.setAttribute(NAME, this.name);
			e.setAttribute(VALUE, this.value);
		return e;
	}

}
