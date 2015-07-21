package webhook.teamcity.settings.entity;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import webhook.teamcity.payload.template.WebHookTemplateFromXml;
import webhook.teamcity.settings.entity.WebHookTemplate.WebHookTemplateFormat;

public class WebHookTemplateJaxHelperTest {

	@Test
	public void testReadString() throws FileNotFoundException, JAXBException {
		WebHookTemplates templatesList =  WebHookTemplateJaxHelper.read("src/test/resources");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(WebHookTemplates.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		//Marshal the employees list in console
		jaxbMarshaller.marshal(templatesList, System.out);
		
	}

//	@Test
//	public void testReadInputStream() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testLoad() {
		Map<String, WebHookTemplateFromXml> templatesMap = new HashMap<String, WebHookTemplateFromXml>(); 
		WebHookTemplates templatesList =  WebHookTemplateJaxHelper.load("src/test/resources", templatesMap);
		
	}

//	@Test
//	public void testWrite() {
//		fail("Not yet implemented");
//	}

}
