package webhook.teamcity.settings.entity;

import javax.xml.bind.JAXBException;

public class WebHookTemplateJaxTestHelper extends WebHookTemplateJaxHelperImpl implements WebHookTemplateJaxHelper {


	@Override
	public void writeTemplates(WebHookTemplates templates, String configFilePath) throws JAXBException {
		// No writes needed in test implementation
	}


}