package webhook.teamcity.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import webhook.teamcity.Loggers;

public class JaxHelperImpl<T> implements JaxHelper<T> {

	@SuppressWarnings("unchecked")
	@Override
	public T readFile(String configFilePath, Class<T> clazz) throws JAXBException, FileNotFoundException {
		JAXBContext context = JAXBContext.newInstance(clazz);
		Unmarshaller um = context.createUnmarshaller();
		File file = new File(configFilePath);
		if (!file.isFile()) {
			throw new FileNotFoundException("File was not a file");
		}
		return (T) um.unmarshal(file);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T readStream(InputStream stream, Class<T> clazz) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(clazz);
		Unmarshaller um = context.createUnmarshaller();
		return (T) um.unmarshal(stream);
	}

	@Override
	public void writeFile(T bean, Class<T> clazz, String configFilePath) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(clazz);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(bean, new File(configFilePath));
		Loggers.SERVER.debug(String.format("JaxHelperImpl :: File written to [%s]", configFilePath));
	}

}
