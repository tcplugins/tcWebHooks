package webhook.teamcity.server.rest.model.template;


import com.intellij.openapi.util.text.StringUtil;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import webhook.teamcity.payload.template.WebHookTemplateFromXml;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.util.BeanContext;
import jetbrains.buildServer.server.rest.APIController;
import webhook.teamcity.server.rest.data.DataProvider;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.data.ProjectFinder;
import jetbrains.buildServer.server.rest.errors.BadRequestException;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import jetbrains.buildServer.server.rest.model.Properties;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.WebLinks;

@XmlRootElement(name = "template")
@XmlType(name = "template", propOrder = {"id", "name", "description", "href", "webUrl"})
@SuppressWarnings("PublicField")
public class Template {
	  @XmlAttribute
	  public String id;

	  @XmlAttribute
	  public String name;

	  @XmlAttribute
	  public String href;

	  @XmlAttribute
	  public String description;

	  @XmlAttribute
	  public String webUrl;

	  /**
	   * This is used only when posting a link to a project
	   */
	  @XmlAttribute public String locator;

	  public Template() {
	  }

	  public Template(@NotNull final WebHookTemplateEntity template, final @NotNull Fields fields, @NotNull final BeanContext beanContext) {
	    id = ValueWithDefault.decideDefault(fields.isIncluded("id"), template.getName());
	    name = ValueWithDefault.decideDefault(fields.isIncluded("name"), template.getName());

	    href = ValueWithDefault.decideDefault(fields.isIncluded("href"), beanContext.getApiUrlBuilder().getHref(template));
	    webUrl = ValueWithDefault.decideDefault(fields.isIncluded("webUrl"), beanContext.getSingletonService(WebLinks.class).getProjectPageUrl(template.getName()));

	    final String descriptionText = template.getTemplateDescription();
	    description = ValueWithDefault.decideDefault(fields.isIncluded("description"), StringUtil.isEmpty(descriptionText) ? null : descriptionText);

	  }

	  @Nullable
	  public static String getFieldValue(final WebHookTemplateEntity template, final String field) {
	    if ("id".equals(field)) {
	      return template.getName();
	    } else if ("description".equals(field)) {
	      return template.getTemplateDescription();
	    } else if ("name".equals(field)) {
	      return template.getName();
	    }
	    throw new NotFoundException("Field '" + field + "' is not supported.  Supported are: id, name, description.");
	  }

	  public static void setFieldValueAndPersist(final WebHookTemplateFromXml template, final String field, final String value, @NotNull final DataProvider dataProvider) {
	    if ("name".equals(field)) {
	      if (StringUtil.isEmpty(value)){
	        throw new BadRequestException("Project name cannot be empty.");
	      }
	      template.setTemplateShortName(value);
	      template.persist();
	      return;
	    } else if ("description".equals(field)) {
	      template.setTemplateDescription(value);
	      template.persist();
	      return;

	    }
	    throw new BadRequestException("Setting field '" + field + "' is not supported. Supported are: name, description, archived");
	  }

	public WebHookTemplateEntity getTemplateFromPosted(TemplateFinder templateFinder) {
		return templateFinder.findTemplateById(this.name);
	}

/*	  @NotNull
	  public WebHookTemplate getProjectFromPosted(@NotNull TemplateFinder templateFinder) {
	    //todo: support posted parentProject fields here
	    String locatorText = "";
	    if (internalId != null) locatorText = "internalId:" + internalId;
	    if (id != null) locatorText += (!locatorText.isEmpty() ? "," : "") + "id:" + id;
	    if (locatorText.isEmpty()) {
	      locatorText = locator;
	    } else {
	      if (locator != null) {
	        throw new BadRequestException("Both 'locator' and 'id' or 'internalId' attributes are specified. Only one should be present.");
	      }
	    }
	    if (jetbrains.buildServer.util.StringUtil.isEmpty(locatorText)){
	      //find by href for compatibility with 7.0
	      if (!jetbrains.buildServer.util.StringUtil.isEmpty(href)){
	        return templateFinder.getTemplate(jetbrains.buildServer.util.StringUtil.lastPartOf(href, '/'));
	      }
	      throw new BadRequestException("No project specified. Either 'id', 'internalId' or 'locator' attribute should be present.");
	    }
	    return templateFinder.getProject(locatorText);
	  }*/
	
}
