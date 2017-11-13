package webhook.teamcity.server.rest.model.template;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.util.text.StringUtil;

import jetbrains.buildServer.server.rest.errors.BadRequestException;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.template.WebHookTemplateFromXml;
import webhook.teamcity.server.rest.WebHookWebLinks;
import webhook.teamcity.server.rest.data.DataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.data.WebHookTemplateConfigWrapper;
import webhook.teamcity.server.rest.data.WebHookTemplateItemConfigWrapper.WebHookTemplateItemRest;
import webhook.teamcity.server.rest.data.WebHookTemplateStates;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateBranchText;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateItem;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateState;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateText;

@XmlRootElement(name = "template")
@XmlType(name = "template", propOrder = { "id", "description", "status", "format", "rank", "href", "webUrl", "defaultTemplate", "preferredDateFormat", "toolTip", "templates" })

public class Template {
	@XmlAttribute
	public String id;

	@XmlAttribute
	public String status;
	
	@XmlAttribute
	public Integer rank;
	
	@XmlAttribute
	public String format;
	
	@XmlAttribute
	public String href;

	@XmlAttribute
	public String description;

	@XmlAttribute
	public String webUrl;
	
	@XmlElement
	public String preferredDateFormat;
	
	@XmlElement
	public String toolTip; 
	
	@XmlElement(required=false)
	public TemplateItem defaultTemplate;
	
	@XmlElement(name = "templateItem") @XmlElementWrapper(name = "templateItems") @Getter
	List<TemplateItem> templates;
	
	@XmlType @Getter @Setter @XmlAccessorType(XmlAccessType.FIELD)
	public static class TemplateText extends BranchTemplateText {
		@XmlAttribute
		public Boolean useTemplateTextForBranch = false;
		
		TemplateText(){
			super();
		}
		
		TemplateText(WebHookTemplateConfig webHookTemplateEntity, String id, final @NotNull Fields fields, @NotNull final BeanContext beanContext){
			super(webHookTemplateEntity, id, fields, beanContext);
			this.href = ValueWithDefault.decideDefault(fields.isIncluded("href"), beanContext.getApiUrlBuilder().getDefaultTemplateTextHref(webHookTemplateEntity));
			if (webHookTemplateEntity.getDefaultTemplate() != null){
				useTemplateTextForBranch = ValueWithDefault.decideDefault(fields.isIncluded("useTemplateTextForBranch", true, true), webHookTemplateEntity.getDefaultTemplate().isUseTemplateTextForBranch());
				this.content = ValueWithDefault.decideDefault(fields.isIncluded("content", false, false), webHookTemplateEntity.getDefaultTemplate().getTemplateContent());
			}
		}
		
		TemplateText(WebHookTemplateConfig webHookTemplateEntity, WebHookTemplateItemRest webHookTemplateItem, String id, final @NotNull Fields fields, @NotNull final BeanContext beanContext){
			super(webHookTemplateEntity, webHookTemplateItem, id, fields, beanContext);
			this.href = ValueWithDefault.decideDefault(fields.isIncluded("href"), beanContext.getApiUrlBuilder().getTemplateItemTextHref(webHookTemplateEntity, webHookTemplateItem));
			if (webHookTemplateItem.getTemplateText() != null){
				useTemplateTextForBranch = ValueWithDefault.decideDefault(fields.isIncluded("useTemplateTextForBranch", true, true), webHookTemplateItem.getTemplateText().isUseTemplateTextForBranch());
				this.content = ValueWithDefault.decideDefault(fields.isIncluded("content", false, false), webHookTemplateItem.getTemplateText().getTemplateContent());
			}
		}
		
				

	}
		
	@XmlType @Data @XmlAccessorType(XmlAccessType.FIELD)
	@NoArgsConstructor
	public static class BranchTemplateText {
		
		@XmlAttribute
		public String href;
		
		@XmlAttribute
		public String webUrl;
		
		@XmlAttribute
		public String content;

		BranchTemplateText(WebHookTemplateConfig webHookTemplateEntity, String id, Fields fields, BeanContext beanContext) {
			this.href = ValueWithDefault.decideDefault(fields.isIncluded("href"), beanContext.getApiUrlBuilder().getDefaultBranchTemplateTextHref(webHookTemplateEntity));
			this.webUrl = ValueWithDefault.decideDefault(fields.isIncluded("webUrl"), beanContext.getSingletonService(WebHookWebLinks.class).getWebHookDefaultBranchTemplateTextUrl(webHookTemplateEntity));
			this.content = ValueWithDefault.decideDefault(fields.isIncluded("content",false, false), webHookTemplateEntity.getDefaultBranchTemplate().getTemplateContent());
		}
		
		BranchTemplateText(WebHookTemplateConfig webHookTemplateEntity, WebHookTemplateItemRest webHookTemplateItem, String id, Fields fields, BeanContext beanContext) {
			this.href = ValueWithDefault.decideDefault(fields.isIncluded("href"), beanContext.getApiUrlBuilder().getTemplateItemBranchTextHref(webHookTemplateEntity, webHookTemplateItem));
			this.webUrl = ValueWithDefault.decideDefault(fields.isIncluded("webUrl"), beanContext.getSingletonService(WebHookWebLinks.class).getWebHookBranchTemplateTextUrl(webHookTemplateEntity, webHookTemplateItem));
			this.content = ValueWithDefault.decideDefault(fields.isIncluded("content",false, false), webHookTemplateItem.getBranchTemplateText().getTemplateContent());
		}
		
	}

	@XmlRootElement(name = "templateItem")
	@XmlType(name = "templateItem", propOrder = { "id", "enabled", "href", "templateText", "branchTemplateText", "parentTemplate", "buildStates"})
	@Data @XmlAccessorType(XmlAccessType.FIELD)
	public static class TemplateItem {
		@XmlElement
		TemplateText templateText;

		@XmlElement
		BranchTemplateText branchTemplateText;

		@XmlAttribute
		Boolean enabled;
		
		@XmlAttribute
		public String id;
		
		@XmlAttribute
		public String href;
		
		@XmlElement
		public TemplateItemParent parentTemplate;
		
		@XmlElement(name = "buildState")	@XmlElementWrapper(name = "buildStates")
		private List<WebHookTemplateStateRest> buildStates = new ArrayList<>();

		public TemplateItem() {
			// empty constructor for JAXB
		}

		/**
		 * Used for Default Template TemplateItem
		 * @param template
		 * @param templateText
		 * @param branchTemplateText
		 * @param id
		 * @param fields
		 * @param beanContext
		 */
		public TemplateItem(WebHookTemplateConfigWrapper template, WebHookTemplateText templateText, WebHookTemplateBranchText branchTemplateText, String id, Fields fields, BeanContext beanContext) {
			this.id = ValueWithDefault.decideDefault(fields.isIncluded("id"), String.valueOf(id));
			this.href = ValueWithDefault.decideDefault(fields.isIncluded("href"), String.valueOf(beanContext.getApiUrlBuilder().getTemplateDefaultItemHref(template.getTemplateConfig())));
			this.templateText = ValueWithDefault.decideDefault(fields.isIncluded("templateText", false, true), new TemplateText(template.getTemplateConfig(), id, fields, beanContext));
			this.branchTemplateText = ValueWithDefault.decideDefault(fields.isIncluded("branchTemplateText", false, true), new BranchTemplateText(template.getTemplateConfig(), id, fields, beanContext));
			this.parentTemplate = ValueWithDefault.decideDefault(fields.isIncluded("parentTemplate", false, true), new TemplateItemParent(template.getTemplateConfig().getId(), template.getTemplateConfig().getTemplateDescription(), beanContext.getApiUrlBuilder().getHref(template.getTemplateConfig())));

			if (fields.isIncluded("buildStates", false, true)) {
				buildStates = new ArrayList<>();
				for (BuildStateEnum state : BuildStateEnum.getNotifyStates()) {
					buildStates.add(
								new WebHookTemplateStateRest(
														state.getShortName(), 
														template.getBuildStatesWithTemplate().isAvailable(state), 
														ValueWithDefault.decideDefault(fields.isIncluded("editable",false, true), false), 
														beanContext.getApiUrlBuilder()
														   .getWebHookTemplateStateUrl(template.getTemplateConfig(), state.getShortName())
													)
												);
					
				}
			}
		}
		/**
		 * Used for Build Event TemplateItems
		 * @param template
		 * @param templateItem
		 * @param id
		 * @param fields
		 * @param beanContext
		 */
		public TemplateItem(WebHookTemplateConfigWrapper template, WebHookTemplateItemRest templateItem, String id, Fields fields, BeanContext beanContext) {
			this.id = ValueWithDefault.decideDefault(fields.isIncluded("id"), String.valueOf(id));
			this.href = ValueWithDefault.decideDefault(fields.isIncluded("href"), String.valueOf(beanContext.getApiUrlBuilder().getTemplateItemHref(template.getTemplateConfig(), templateItem)));
			this.templateText = new TemplateText(template.getTemplateConfig(), templateItem, id, fields, beanContext);
			this.branchTemplateText = new BranchTemplateText(template.getTemplateConfig(), templateItem, id, fields, beanContext);
			this.parentTemplate = ValueWithDefault.decideDefault(fields.isIncluded("parentTemplate", false, true), new TemplateItemParent(template.getTemplateConfig().getId(), template.getTemplateConfig().getTemplateDescription(), beanContext.getApiUrlBuilder().getHref(template.getTemplateConfig())));
			this.buildStates.clear();
			for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
				WebHookTemplateStateRest myState = new WebHookTemplateStateRest(state.getShortName(), 
														false,
														ValueWithDefault.decideDefault(fields.isIncluded("editable",false, true), template.getBuildStatesWithTemplate().isAvailable(state)),
														beanContext.getApiUrlBuilder()
																   .getWebHookTemplateItemStateUrl(template.getTemplateConfig(), templateItem, state.getShortName()));
				for (WebHookTemplateState itemState: templateItem.getStates()){
					if (state.getShortName().equals(itemState.getType())){
						myState.setEnabled(itemState.isEnabled());
						if(itemState.isEnabled()) {
							myState.setEditable(ValueWithDefault.decideDefault(fields.isIncluded("editable",false, true), true));
						}
					}
				}
				this.buildStates.add(myState);
			}
		}
		
		public WebHookTemplateStateRest findConfigForBuildState(String buildStateShortName) {
			for (WebHookTemplateStateRest itemState: this.getBuildStates()){
				if (buildStateShortName.equals(itemState.getType())){
					return itemState;
				}
			}
			return null;
		}
		
	}
	
	@XmlRootElement
	@XmlType (name = "parentTemplate", propOrder = { "id", "description", "href" }) 
	@Getter @Setter @XmlAccessorType(XmlAccessType.FIELD)
	@NoArgsConstructor // empty constructor for JAXB
	@AllArgsConstructor
	public static class TemplateItemParent {
		String id;
		String description;
		String href;
	}
	
	@XmlRootElement (name = "buildState")
	@XmlType (name = "buildState", propOrder = { "type", "enabled", "editable", "href" }) 
	@Getter @Setter @XmlAccessorType(XmlAccessType.FIELD)
	@NoArgsConstructor // empty constructor for JAXB
	public static class WebHookTemplateStateRest {
		@NotNull 
		String type;
		
		boolean enabled;
		
		Boolean editable;
		
		String href;
		
		public WebHookTemplateStateRest(String shortName, boolean enabled, Boolean editable, String href) {
			this.type = shortName;
			this.enabled = enabled;
			this.editable = editable;
			this.href = href;
		}
		
		public WebHookTemplateStateRest(WebHookTemplateItemRest templateItem, String type, WebHookTemplateStates states, Fields fields, BeanContext beanContext){
			for (WebHookTemplateState itemState: templateItem.getStates()){
				if (type.equals(itemState.getType())){
					this.enabled = itemState.isEnabled();
				}
			}
			this.editable = ValueWithDefault.decideDefault(fields.isIncluded("editable",false, true), states.isAvailable(type));
			this.type = ValueWithDefault.decideDefault(fields.isIncluded("type"), type);
		}
		
	}

	/**
	 * This is used only when posting a link to a template
	 */
	@XmlAttribute
	public String locator;

	public Template() {
	}

	public Template(@NotNull final WebHookTemplateConfigWrapper templateWrapper,
			final @NotNull Fields fields, @NotNull final BeanContext beanContext) {
		
		WebHookTemplateConfig template = templateWrapper.getTemplateConfig(); 
		
		id = ValueWithDefault.decideDefault(fields.isIncluded("id"),
				template.getId());
		description = ValueWithDefault.decideDefault(
				fields.isIncluded("description"),
				template.getTemplateDescription());
		
		status = ValueWithDefault.decideDefault(fields.isIncluded("status"),
				templateWrapper.getStatus().toString());
		
		format = ValueWithDefault.decideDefault(fields.isIncluded("format"),
				template.getFormat());
		
		rank = ValueWithDefault.decideDefault(fields.isIncluded("rank"),
				Integer.valueOf(template.getRank()));
		
		preferredDateFormat = ValueWithDefault.decideDefault(fields.isIncluded("preferredDateFormat"),
				template.getPreferredDateTimeFormat());
		
		toolTip = ValueWithDefault.decideDefault(fields.isIncluded("toolTip"),
				template.getTemplateToolTip());

		href = ValueWithDefault.decideDefault(fields.isIncluded("href"), beanContext.getApiUrlBuilder().getHref(template));
		
		webUrl = ValueWithDefault.decideDefault(fields.isIncluded("webUrl"), beanContext.getSingletonService(WebHookWebLinks.class).getWebHookTemplateUrl(template));
		
		if (template.getDefaultTemplate() != null){
			defaultTemplate = ValueWithDefault.decideDefault(
					fields.isIncluded("defaultTemplate", false, true),new TemplateItem(templateWrapper, template.getDefaultTemplate(), template.getDefaultBranchTemplate(), "defaultTemplate", fields, beanContext)); 
		}

		if (fields.isIncluded("templateItem", false, true)){
			templates = new ArrayList<>();
			
			if (template.getTemplates() != null){
				for (WebHookTemplateItem templateItem: template.getTemplates().getTemplates()){
					templates.add(new TemplateItem(templateWrapper, new WebHookTemplateItemRest(templateItem), templateItem.getId().toString(), fields, beanContext));
				}	
			}
		}

	}

	@Nullable
	public static String getFieldValue(final WebHookTemplateConfig template,
			final String field) {
		if ("id".equals(field)) {
			return template.getId();
		} else if ("description".equals(field)) {
			return template.getTemplateDescription();
		} else if ("name".equals(field)) {
			return template.getId();
		}
		throw new NotFoundException("Field '" + field
				+ "' is not supported.  Supported are: id, name, description.");
	}

	public static void setFieldValueAndPersist(
			final WebHookTemplateFromXml template, final String field,
			final String value, @NotNull final DataProvider dataProvider) {
		if ("name".equals(field)) {
			if (StringUtil.isEmpty(value)) {
				throw new BadRequestException("Template name cannot be empty.");
			}
			template.setTemplateId(value);
			template.persist();
			return;
		} else if ("description".equals(field)) {
			template.setTemplateDescription(value);
			template.persist();
			return;

		}
		throw new BadRequestException(
				"Setting field '"
						+ field
						+ "' is not supported. Supported are: name, description, archived");
	}

	public WebHookTemplateConfig getTemplateFromPosted(
			TemplateFinder templateFinder) {
		return templateFinder.findTemplateById(this.id).getTemplateConfig();
	}
	
	@XmlRootElement
	@XmlType (name = "status", propOrder = { "status", "description" }) 
	@Getter @Setter @XmlAccessorType(XmlAccessType.FIELD)
	public static class TemplateStatusRest{
		
		@XmlAttribute
		String description;
		
		@XmlElement
		String status;
		
		public TemplateStatusRest(){
		}
		
		public TemplateStatusRest(WebHookTemplateManager.TemplateState state){
			this.status = state.toString();
			this.description = state.getDescription();
		}
	}

}
