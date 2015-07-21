package webhook.teamcity.payload;

import org.jdom.Element;

public class WebHookTemplateContent {
	String templateState;
	String templateText;
	boolean enabled;
	
	public static final String XML_ELEMENT_NAME = "template";
	public static final String STATE = "build-state";
	public static final String TEMPLATE = "template-text";
	public static final String ENABLED = "enabled";
	
	public static WebHookTemplateContent create(String templateState, String templateText, boolean enabled){
		WebHookTemplateContent t = new WebHookTemplateContent();
		t.templateState = templateState;
		t.templateText = templateText;
		t.enabled = enabled;
		return t;
	}
	
	public String getTemplateText() {
		return templateText;
	}

	public Element getAsElement() {
		Element e = new Element(XML_ELEMENT_NAME);
			e.setAttribute(STATE, this.templateState);
			e.setAttribute(TEMPLATE, this.templateText);
			e.setAttribute(ENABLED, String.valueOf(this.enabled));
		return e;
	}
	
	public WebHookTemplateContent copy (){
		WebHookTemplateContent t = new WebHookTemplateContent();
		t.templateState = this.templateState;
		t.templateText = this.templateText;
		t.enabled = this.enabled;
		return t;
	}
}