package webhook.teamcity.server.rest.model.template;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.server.rest.errors.BadRequestException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import lombok.Getter;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.data.WebHookTemplateConfigWrapper;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.config.WebHookTemplateConfig;


@XmlRootElement
public class Templates {
	
	  @XmlElement(name = "template") @Getter
	  private List<Template> templates = new ArrayList<>();

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

	  public Templates(@NotNull final List<WebHookTemplateConfigWrapper> templateObjects, @Nullable final PagerData pagerData, final @NotNull Fields fields, @NotNull final BeanContext beanContext) {
	    if (fields.isIncluded("template", false, true)){
	      templates = ValueWithDefault.decideDefault(fields.isIncluded("template"), new ValueWithDefault.Value<List<Template>>() {
	        public List<Template> get() {
	          final ArrayList<Template> result = new ArrayList<>(templateObjects.size());
	          final Fields nestedField = new Fields("id,name,description,status,href,webUrl");
	          for (WebHookTemplateConfigWrapper template : templateObjects) {
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
	  public List<WebHookTemplateConfig> getTemplatesFromPosted(@NotNull TemplateFinder templateFinder) {
	    if (templates == null) {
	      throw new BadRequestException("List of projects should be supplied");
	    }
	    final ArrayList<WebHookTemplateConfig> result = new ArrayList<>(templates.size());
	    for (Template template : templates) {
	      result.add(template.getTemplateFromPosted(templateFinder));
	    }
	    return result;
	  }

}
