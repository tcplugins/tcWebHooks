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
	public abstract WebHookTemplates read(String configFilePath)
			throws JAXBException, FileNotFoundException;

	/**
	 * Read saved configuration from stream
	 * 
	 * @return Suppliers bean
	 * @throws JAXBException
	 */
	public abstract WebHookTemplates read(InputStream stream)
			throws JAXBException;

	/**
	 * Write suppliers bean to configuration file
	 * 
	 * @throws JAXBException
	 */
	public abstract void write(WebHookTemplates templates, String configFilePath)
			throws JAXBException;

}
