package webhook.teamcity.settings.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement 
@XmlAccessorType(XmlAccessType.FIELD)
@Data @NoArgsConstructor
public class WebHookTemplateConfig {

	String name;
	boolean enabled;
	int rank;
	WebHookTemplateText defaultTemplate;
	WebHookTemplateBranchText defaultBranchTemplate;
	String templateDescription;
	String templateToolTip;
	String preferredDateTimeFormat;
	String format;
	WebHookTemplateItems templates = new WebHookTemplateItems();
	

	public WebHookTemplateConfig(String templateName, boolean templateEnabled) {
		this.name = templateName;
		this.enabled = templateEnabled;
	}

	@XmlRootElement 
	@XmlAccessorType(XmlAccessType.FIELD)
	@Data @AllArgsConstructor @NoArgsConstructor
	public static class WebHookTemplateText {
		boolean useTemplateTextForBranch;
		String templateContent;
		
		public WebHookTemplateText(String templateText) {
			this(false, templateText);
		}
	}
	
	@XmlRootElement 
	@XmlAccessorType(XmlAccessType.FIELD)
	@Data @AllArgsConstructor @NoArgsConstructor
	public static class WebHookTemplateBranchText {
		String templateContent;
	}

	@XmlRootElement 
	@XmlAccessorType(XmlAccessType.FIELD)
	@Data @AllArgsConstructor @NoArgsConstructor
	public static class WebHookTemplateItems {
		Integer maxId;
		List<WebHookTemplateItem> templates = new ArrayList<>();
		
		public WebHookTemplateItem getTemplateItem(int id) {
			for (WebHookTemplateItem item : this.templates) {
				if (item.getId().equals(id)){
					return item;
				}
			}
			return null;
		}
	}
	
	@XmlRootElement 
	@XmlAccessorType(XmlAccessType.FIELD)
	@Data @AllArgsConstructor @NoArgsConstructor
	public static class WebHookTemplateItem {
		WebHookTemplateText templateText;
		WebHookTemplateBranchText branchTemplateText;
		boolean enabled;
		Integer id;
		List<WebHookTemplateState> states = new ArrayList<>();
	}
	
	@XmlRootElement 
	@XmlAccessorType(XmlAccessType.FIELD)
	@Data @AllArgsConstructor @NoArgsConstructor
	public static class WebHookTemplateState {
		String type;
		boolean enabled;
	}
}
