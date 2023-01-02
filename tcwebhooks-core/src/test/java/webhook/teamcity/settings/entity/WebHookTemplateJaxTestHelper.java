package webhook.teamcity.settings.entity;

import javax.xml.bind.JAXBException;

/**
 * A No-op JAX helper for when reading/writing is not required. 
 */
public class WebHookTemplateJaxTestHelper extends WebHookTemplateJaxHelperImpl implements WebHookTemplateJaxHelper {


	@Override
	public void writeTemplate(WebHookTemplateEntity templates, String configFilePath) throws JAXBException {
		
	}
	
	@Override
	public void writeTemplates(WebHookTemplates templates, String configFilePath) throws JAXBException {
		
	}


}
