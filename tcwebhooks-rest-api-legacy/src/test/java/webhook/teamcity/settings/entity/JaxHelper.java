package webhook.teamcity.settings.entity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JaxHelper<T> {
	
	@SuppressWarnings("unchecked")
	@Nullable
	public T read(@NotNull String configFilePath, Class<T>  jaxbClass)
			throws JAXBException, FileNotFoundException {
		JAXBContext context = JAXBContext.newInstance(jaxbClass);
		Unmarshaller um = context.createUnmarshaller();
		File file = new File(configFilePath);

		return (T) um.unmarshal(file);
	}

	@SuppressWarnings("unchecked")
	@NotNull
	public T read(@NotNull InputStream stream, Class<T>  jaxbClass)
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(jaxbClass);
		Unmarshaller um = context.createUnmarshaller();
		return (T) um.unmarshal(stream);
	}

	public void write(@NotNull T jaxContent, Class<T>  jaxbClass, 
			@NotNull String configFilePath) throws JAXBException {
		
		JAXBContext context = JAXBContext.newInstance(jaxbClass);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(jaxContent, new File(configFilePath));
	}



}