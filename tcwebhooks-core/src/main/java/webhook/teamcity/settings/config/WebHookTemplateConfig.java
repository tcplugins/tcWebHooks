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

	String id;
	boolean enabled;
	int rank;
	WebHookTemplateText defaultTemplate;
	WebHookTemplateBranchText defaultBranchTemplate;
	String templateDescription;
	String templateToolTip;
	String preferredDateTimeFormat;
	String format;
	String projectInternalId;
	WebHookTemplateItems templates = new WebHookTemplateItems();
	

	public WebHookTemplateConfig(String id, boolean templateEnabled) {
		this.id = id;
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
		
		public int addTemplateItem(WebHookTemplateItem templateItem) {
			templateItem.id = maxId++;
			this.templates.add(templateItem);
			return templateItem.id;
		}
		
		public WebHookTemplateItem getTemplateItem(int id) {
			for (WebHookTemplateItem item : this.templates) {
				if (item.getId().equals(id)){
					return item;
				}
			}
			return null;
		}
		public void deleteTemplateItem(int id) {
			for (WebHookTemplateItem item : new ArrayList<>(this.templates)) {
				if (item.getId().equals(id)){
					this.templates.remove(item);
					break;
				}
			}
		}
	}
	
	@XmlRootElement 
	@XmlAccessorType(XmlAccessType.FIELD)
	@Data @AllArgsConstructor @NoArgsConstructor
	public static class WebHookTemplateItem {
		WebHookTemplateText templateText;
		WebHookTemplateBranchText branchTemplateText;
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
