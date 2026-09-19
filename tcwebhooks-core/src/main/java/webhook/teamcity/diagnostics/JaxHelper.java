package webhook.teamcity.diagnostics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JaxHelper<T> {
	
	@Nullable
	public T read(@NotNull String configFilePath, Class<T>  jaxbClass)
			throws JAXBException, FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		return read(new FileInputStream(configFilePath), jaxbClass);
	}

	@SuppressWarnings("unchecked")
	@NotNull
	public T read(@NotNull InputStream stream, Class<T>  jaxbClass)
			throws JAXBException, XMLStreamException, FactoryConfigurationError {
		
		XMLStreamReader xsr = XMLInputFactory.newFactory().createXMLStreamReader(stream);
		XMLReaderWithoutNamespace xr = new XMLReaderWithoutNamespace(xsr);
		return (T) JAXBContext.newInstance(jaxbClass).createUnmarshaller().unmarshal(xr);
	}

	public void write(@NotNull T jaxContent, Class<T>  jaxbClass, 
			@NotNull String configFilePath) throws JAXBException {
		
		JAXBContext context = JAXBContext.newInstance(jaxbClass);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(jaxContent, new File(configFilePath));
	}

	/**
	 * An XML StreamReader that overrides and returns empty an namespace
	 * and empty namespace URL so that if an XML file has a name space, it 
	 * can be ignored.
	 */
	class XMLReaderWithoutNamespace extends StreamReaderDelegate {
		public XMLReaderWithoutNamespace(XMLStreamReader reader) {
			super(reader);
		}

		@Override
		public String getAttributeNamespace(int arg0) {
			return "";
		}

		@Override
		public String getNamespaceURI() {
			return "";
		}
	}


}