package webhook.teamcity.settings.entity;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;

import webhook.teamcity.payload.template.WebHookTemplateFromXml;

public class WebHookTemplateJaxHelperTest {

	@Test
	public void testReadString() throws FileNotFoundException, JAXBException {
		WebHookTemplates templatesList =  WebHookTemplateJaxHelper.read("src/test/resources/webhook-templates.xml");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(WebHookTemplates.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		//Marshal the employees list in console
		jaxbMarshaller.marshal(templatesList, System.out);
		
	}
	
	
	@Test
	public void testReadStringAndOutputWithCData() throws FileNotFoundException, JAXBException, ParserConfigurationException, TransformerException {
		WebHookTemplates templatesList =  WebHookTemplateJaxHelper.read("src/test/resources/webhook-templates.xml");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(WebHookTemplates.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
		// Create an empty DOM document
		// DocumentBuilderFactory is not thread-safe
		DocumentBuilderFactory docBuilderFactory = 
		    DocumentBuilderFactory.newInstance();
		Document document = 
		    docBuilderFactory.newDocumentBuilder().newDocument();
		
		//jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		//Marshal the employees list in console
		jaxbMarshaller.marshal(templatesList, document);
		
		
		// Transform the DOM to the output stream
		// TransformerFactory is not thread-safe
		TransformerFactory transformerFactory = 
		    TransformerFactory.newInstance();
		Transformer nullTransformer = transformerFactory.newTransformer();
		nullTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
		nullTransformer.setOutputProperty(
		    OutputKeys.CDATA_SECTION_ELEMENTS,
		     "template-description default-template default-branch-template template-text branch-template-text");
		nullTransformer.transform(new DOMSource(document),
		     new StreamResult(System.out));
		
	}
	
	
	

//	@Test
//	public void testReadInputStream() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testLoad() {
		Map<String, WebHookTemplateFromXml> templatesMap = new HashMap<>();
		WebHookTemplates templatesList =  WebHookTemplateJaxHelper.load("src/test/resources/webhook-templates.xml", templatesMap);
	}

//	@Test
//	public void testWrite() {
//		fail("Not yet implemented");
//	}

}
