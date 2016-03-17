package webhook.teamcity.server.rest.model.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.data.BuildFinder;
import jetbrains.buildServer.server.rest.data.ProjectFinder;
import jetbrains.buildServer.server.rest.data.QueuedBuildFinder;
import jetbrains.buildServer.server.rest.errors.BadRequestException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import jetbrains.buildServer.server.rest.model.build.Build;
import jetbrains.buildServer.server.rest.model.project.Project;
import jetbrains.buildServer.server.rest.util.DefaultValueAware;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;


@XmlRootElement(name = "templates")
@XmlType(name = "templates")
public class Templates {
	@XmlElement(name = "template")
	private List<Template> templates = new ArrayList<Template>();

	  @XmlAttribute
	  public Integer count;

	  @XmlAttribute
	  @Nullable
	  public String href;

	  @XmlAttribute(required = false)
	  @Nullable
	  public String nextHref;

	  @XmlAttribute(required = false)
	  @Nullable
	  public String prevHref;

	  public Templates() {
	  }

	  public Templates(@NotNull final List<WebHookTemplateEntity> templateObjects, @Nullable final PagerData pagerData, final @NotNull Fields fields, @NotNull final BeanContext beanContext) {
	    if (fields.isIncluded("template", false, true)){
	      templates = ValueWithDefault.decideDefault(fields.isIncluded("project"), new ValueWithDefault.Value<List<Template>>() {
	        public List<Template> get() {
	          final ArrayList<Template> result = new ArrayList<Template>(templateObjects.size());
	          final Fields nestedField = fields.getNestedField("project");
	          for (WebHookTemplateEntity template : templateObjects) {
	            result.add(new Template(template, nestedField, beanContext));
	          }
	          return result;
	        }
	      });
	    }else{
	    	templates = null;
	    }
	    if (pagerData != null) {
	      href = ValueWithDefault.decideDefault(fields.isIncluded("href"), beanContext.getApiUrlBuilder().transformRelativePath(pagerData.getHref()));
	      nextHref = ValueWithDefault
	        .decideDefault(fields.isIncluded("nextHref"), pagerData.getNextHref() != null ? beanContext.getApiUrlBuilder().transformRelativePath(pagerData.getNextHref()) : null);
	      prevHref = ValueWithDefault
	        .decideDefault(fields.isIncluded("prevHref"), pagerData.getPrevHref() != null ? beanContext.getApiUrlBuilder().transformRelativePath(pagerData.getPrevHref()) : null);
	    }
	    count = ValueWithDefault.decideIncludeByDefault(fields.isIncluded("count"), templateObjects.size());
	  }

	  @NotNull
	  public List<WebHookTemplateEntity> getTemplatesFromPosted(@NotNull TemplateFinder templateFinder) {
	    if (templates == null) {
	      throw new BadRequestException("List of projects should be supplied");
	    }
	    final ArrayList<WebHookTemplateEntity> result = new ArrayList<WebHookTemplateEntity>(templates.size());
	    for (Template template : templates) {
	      result.add(template.getTemplateFromPosted(templateFinder));
	    }
	    return result;
	  }

}
