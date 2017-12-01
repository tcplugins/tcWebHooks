package webhook.teamcity.settings.entity;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

public interface WebHookTemplateJaxHelper {

	/**
	 * Read saved configuration from file
	 * 
	 * @return {@link WebHookTemplates} bean
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	public abstract WebHookTemplateEntity readTemplate(String configFilePath)
			throws JAXBException, FileNotFoundException;

	/**
	 * Read saved configuration from stream
	 * 
	 * @return Suppliers bean
	 * @throws JAXBException
	 */
	public abstract WebHookTemplateEntity readTemplate(InputStream stream)
			throws JAXBException;

	/**
	 * Write suppliers bean to configuration file
	 * 
	 * @throws JAXBException
	 */
	public abstract void writeTemplate(WebHookTemplateEntity templates, String configFilePath)
			throws JAXBException;
	
	/**
	 * Read saved configuration from file
	 * 
	 * @return {@link WebHookTemplates} bean
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	public abstract WebHookTemplates readTemplates(String configFilePath)
			throws JAXBException, FileNotFoundException;
	
	/**
	 * Read saved configuration from stream
	 * 
	 * @return Suppliers bean
	 * @throws JAXBException
	 */
	public abstract WebHookTemplates readTemplates(InputStream stream)
			throws JAXBException;
	
	/**
	 * Write suppliers bean to configuration file
	 * 
	 * @throws JAXBException
	 */
	public abstract void writeTemplates(WebHookTemplates templates, String configFilePath)
			throws JAXBException;

}
