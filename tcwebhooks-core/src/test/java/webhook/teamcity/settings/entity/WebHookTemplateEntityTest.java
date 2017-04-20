package webhook.teamcity.settings.entity;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItem;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItems;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateText;

public class WebHookTemplateEntityTest {

	@Test
	public void testOutput() throws JAXBException {
		WebHookTemplateEntity e = new WebHookTemplateEntity();
		
		List<WebHookTemplateEntity.WebHookTemplateItem> items = new ArrayList<WebHookTemplateEntity.WebHookTemplateItem>();
		
		WebHookTemplateItem hookTemplateItem = new WebHookTemplateItem();
		hookTemplateItem.id = 8;
		hookTemplateItem.templateText = new WebHookTemplateText();
		hookTemplateItem.templateText.setTemplateContent("some template text");
		items.add(hookTemplateItem);

		e.templates = new WebHookTemplateItems();
		e.templates.setMaxId(9);
		e.templates.setTemplates(items);

		JAXBContext jaxbContext = JAXBContext.newInstance(WebHookTemplateEntity.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		//Marshal the employees list in console
		jaxbMarshaller.marshal(e, System.out);
	}
	
	@Test
	public void testLoad() throws JAXBException, FileNotFoundException {
		
		WebHookTemplates templatesList =  new WebHookTemplateJaxHelperImpl().read("src/test/resources/testXmlTemplateWithTemplateIds/config/webhook-templates.xml");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(WebHookTemplates.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		//Marshal the employees list in console
		jaxbMarshaller.marshal(templatesList, System.out);
		
//		WebHookTemplateEntity e = new WebHookTemplateEntity();
//		
//		List<WebHookTemplateEntity.WebHookTemplateItem> items = new ArrayList<WebHookTemplateEntity.WebHookTemplateItem>();
//		
//		WebHookTemplateItem hookTemplateItem = new WebHookTemplateItem();
//		hookTemplateItem.id = 8;
//		hookTemplateItem.templateText = new WebHookTemplateText();
//		hookTemplateItem.templateText.templateContent = "some template text";
//		items.add(hookTemplateItem);
//		
//		e.templates = new WebHookTemplateItems();
//		e.templates.maxId = 9;
//		e.templates.setTemplates(items);
//		
//		JAXBContext jaxbContext = JAXBContext.newInstance(WebHookTemplateEntity.class);
//		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
//		
//		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//		
//		//Marshal the employees list in console
//		jaxbMarshaller.marshal(e, System.out);
	}

}
