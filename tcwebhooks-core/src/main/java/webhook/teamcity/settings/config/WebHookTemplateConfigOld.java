package webhook.teamcity.settings.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@XmlRootElement(name = "webhook-template")
public interface WebHookTemplateConfigOld {
	
	@NotNull
	@XmlAttribute(name="name")
	public String getName();
	public void setName(String name);

	@XmlAttribute
	public boolean isEnabled();
	public void setEnabled(boolean enabled);
	
	@XmlAttribute
	public int getRank();
	public void setRank(int rank);

	@XmlElement(name="default-template")
	public WebHookTemplateText getDefaultTemplate();
	public void setDefaultTemplate(WebHookTemplateText defaultTemplate);

	@XmlElement(name="default-branch-template")
	public WebHookTemplateBranchText getDefaultBranchTemplate();
	public void setDefaultBranchTemplate(WebHookTemplateBranchText defaultBranchTemplate);

	@NotNull 
	@XmlElement (name="template-description")
	public String getTemplateDescription();
	public void setTemplateDescription(String templateDescription) ;

	@XmlElement (name="template-tool-tip")
	public String getTemplateToolTip();
	public void setTemplateToolTip(String templateToolTip);

	@XmlElement (name="preferred-date-format")
	public String getPreferredDateTimeFormat();
	public void setPreferredDateTimeFormat(String preferredDateTimeFormat);

	@XmlElement(name="format") @XmlElementWrapper(name="formats")
	public List<WebHookTemplateFormat> getFormats();
	public void setFormats(List<WebHookTemplateFormat> formats);

	@XmlElement(name="templates")
	public WebHookTemplateItems getTemplates() ;
	public void setTemplates(WebHookTemplateItems templates);
	
	@XmlType(name="templates")
	public static interface WebHookTemplateItems {

		@XmlAttribute(name="max-id")
	    public Integer getMaxId();
		public void setMaxId(Integer maxId);
		
		@XmlElements(@XmlElement(name="template"))
		public List<WebHookTemplateItem> getTemplates();
		public void setTemplates(List<WebHookTemplateItem> templates);
	}
	
	@XmlType(name = "format")
	public static interface WebHookTemplateFormat {
		
		@XmlAttribute
		public String getName();
		public void setName(String name);

		@XmlAttribute
		public boolean isEnabled();
		public void setEnabled(boolean enabled);
		
	}
	
	@XmlType
	public static interface WebHookTemplateText {
		
		@XmlAttribute (name="use-for-branch-template")
		public boolean isUseTemplateTextForBranch();
		public void setUseTemplateTextForBranch(boolean useTemplateTextForBranch);

		@XmlValue
		public String getTemplateContent();
		public void setTemplateContent(String templateContent);
		
	}

	@XmlType 
	public static interface WebHookTemplateBranchText {
	
		@XmlValue
		public String getTemplateContent();
		public void setTemplateContent(String templateContent);
		
	}
	
	@XmlRootElement
	@XmlType(name = "template")
	public static interface WebHookTemplateItem {
		
		@NotNull 
		@XmlElement(name="template-text")
		public WebHookTemplateText getTemplateText();
		public void setTemplateText(WebHookTemplateText templateText);
		
		@XmlElement(name="branch-template-text")
		public WebHookTemplateBranchText getBranchTemplateText();
		public void setBranchTemplateText(WebHookTemplateBranchText branchTemplateText);
		
		@XmlAttribute
		public boolean isEnabled();
		public void setEnabled(boolean enabled);
		
		@XmlAttribute @Nullable
		public Integer getId();
		public void setId(Integer id);
		
		@XmlElement(name="state") @XmlElementWrapper(name="states")
		public List<WebHookTemplateState> getStates();
		public void setStates(List<WebHookTemplateState> states);

		
	}
	
	@XmlType(name = "state")
	public static interface WebHookTemplateState {
		
		@XmlAttribute(name="type")
		public String getType();
		public void setType(String type);
		
		@XmlAttribute
		public boolean isEnabled();
		public void setEnabled(boolean enabled);
		
	}
	
}
