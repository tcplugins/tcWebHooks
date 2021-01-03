package webhook.teamcity.statistics;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

public interface JaxHelper<T> {

	/**
	 * Read saved configuration from file
	 * 
	 * @return T bean
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	public abstract T readFile(String configFilePath, Class<T> clazz)
			throws JAXBException, FileNotFoundException;

	/**
	 * Read saved configuration from stream
	 * 
	 * @return T bean
	 * @throws JAXBException
	 */
	public abstract T readStream(InputStream stream, Class<T> clazz)
			throws JAXBException;

	/**
	 * Write T bean to configuration file
	 * 
	 * @throws JAXBException
	 */
	public abstract void writeFile(T bean, Class<T> clazz, String configFilePath) throws JAXBException;

}