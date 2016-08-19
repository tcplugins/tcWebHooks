package webhook.teamcity.settings.entity;

import javax.xml.bind.JAXBException;

import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;
import webhook.teamcity.settings.entity.WebHookTemplates;

public class WebHookTemplateJaxTestHelper extends WebHookTemplateJaxHelperImpl implements WebHookTemplateJaxHelper {


	@Override
	public void write(WebHookTemplates templates, String configFilePath) throws JAXBException {
		
	}


}
