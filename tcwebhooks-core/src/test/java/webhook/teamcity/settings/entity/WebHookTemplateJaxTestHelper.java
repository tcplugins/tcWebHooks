package webhook.teamcity.settings.entity;

import javax.xml.bind.JAXBException;

/**
 * A JAX helper that overrides the write methods with no-ops. Supports Read (from parent class) but does nothing for writes.
 * Useful for tests when writing is not required. 
 */
public class WebHookTemplateJaxTestHelper extends WebHookTemplateJaxHelperImpl implements WebHookTemplateJaxHelper {


	@Override
	public void writeTemplate(WebHookTemplateEntity templates, String configFilePath) throws JAXBException {
		
	}
	
	@Override
	public void writeTemplates(WebHookTemplates templates, String configFilePath) throws JAXBException {
		
	}


}
