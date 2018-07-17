package webhook.teamcity.testing;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;

/**
 * 
 * A NoOp JaxHelper so that any changes we make to a template are not peristed anywhere.
 * This is used by the testing code when generating a Template on the fly for test webhook execution. 
 * 
 * @author netwolfuk
 *
 */
public class  NoOpJaxHelper implements WebHookTemplateJaxHelper {

	/**
	 * Not used.
	 */
	@Override
	public WebHookTemplateEntity readTemplate(String configFilePath) throws JAXBException, FileNotFoundException {
		return null;
	}

	/**
	 * Not used
	 */
	@Override
	public WebHookTemplateEntity readTemplate(InputStream stream) throws JAXBException {
		return null;
	}

	/**
	 * Not used
	 */
	@Override
	public void writeTemplate(WebHookTemplateEntity templates, String configFilePath) throws JAXBException {
		// Not used
	}

	/**
	 * Not used
	 */
	@Override
	public WebHookTemplates readTemplates(String configFilePath) throws JAXBException, FileNotFoundException {
		return null;
	}

	/**
	 * Not Used
	 */
	@Override
	public WebHookTemplates readTemplates(InputStream stream) throws JAXBException {
		return null;
	}

	/**
	 * NoOp
	 */
	@Override
	public void writeTemplates(WebHookTemplates templates, String configFilePath) throws JAXBException {
		// No Op
	}
	
}
