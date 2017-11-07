package webhook.teamcity.settings.entity;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

/**
 * Tests for the migration from 
 * <pre>
 * &lt;template&gt;
 * &nbsp;&lt;formats&gt;
 * &nbsp;&nbsp;&lt;format name="jsonTemplate" enabled="true"&gt;
 * &nbsp;&lt;/formats&gt;
 * &lt;/template&gt;
 * </pre> to <pre> 
 * &nbsp;&lt;template format="jsonTemplate"&gt;
 * &nbsp;&nbsp;...
 * &nbsp;&lt;/template&gt;
 * </pre>
 *
 */
public class WebHookTemplateFormatMigrationTest {

	@Test
	public void testLoadOfOldFormatsListDoesNotContainFormatsTagWhenSerialised() throws JAXBException, FileNotFoundException {
		ByteArrayOutputStream outputXml = new ByteArrayOutputStream();
		WebHookTemplates templatesList =  new WebHookTemplateJaxHelperImpl().read("src/test/resources/testXmlTemplateWithTemplateIds/config/webhook-templates.xml");

		JAXBContext jaxbContext = JAXBContext.newInstance(WebHookTemplates.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(templatesList, outputXml);
		
		String xml = new String(outputXml.toByteArray(), StandardCharsets.UTF_8);
		assertFalse(xml.contains("<formats"));
	}
	
	@Test
	public void testLoadOfNewFormatDoesNotContainFormatsTagWhenSerialised() throws JAXBException, FileNotFoundException {
		ByteArrayOutputStream outputXml = new ByteArrayOutputStream();
		WebHookTemplates templatesList =  new WebHookTemplateJaxHelperImpl().read("src/test/resources/webhook-templates.xml");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(WebHookTemplates.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(templatesList, outputXml);
		
		String xml = new String(outputXml.toByteArray(), StandardCharsets.UTF_8);
		assertFalse(xml.contains("<formats"));
	}
	
	@Test
	public void testLoadOfOldNameDoesNotContainNameAttributeWhenSerialised() throws JAXBException, FileNotFoundException {
		ByteArrayOutputStream outputXml = new ByteArrayOutputStream();
		WebHookTemplates templatesList =  new WebHookTemplateJaxHelperImpl().read("src/test/resources/testXmlTemplateWithTemplateIds/config/webhook-templates.xml");

		JAXBContext jaxbContext = JAXBContext.newInstance(WebHookTemplates.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(templatesList, outputXml);
		
		String xml = new String(outputXml.toByteArray(), StandardCharsets.UTF_8);
		System.out.println(xml);
		assertFalse(xml.contains(" name=\""));
	}

	@Test
	public void testLoadOfNewIdDoesNotContainNameAttributeWhenSerialised() throws JAXBException, FileNotFoundException {
		ByteArrayOutputStream outputXml = new ByteArrayOutputStream();
		WebHookTemplates templatesList =  new WebHookTemplateJaxHelperImpl().read("src/test/resources/webhook-templates.xml");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(WebHookTemplates.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(templatesList, outputXml);
		
		String xml = new String(outputXml.toByteArray(), StandardCharsets.UTF_8);
		assertFalse(xml.contains(" name=\""));
	}
	
}
