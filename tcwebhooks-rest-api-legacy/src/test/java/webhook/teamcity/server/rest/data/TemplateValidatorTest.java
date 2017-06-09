package webhook.teamcity.server.rest.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import webhook.teamcity.server.rest.model.template.Template.TemplateItem;
import webhook.teamcity.server.rest.model.template.TemplateValidationResult;
import webhook.teamcity.settings.entity.JaxHelper;

public class TemplateValidatorTest {

	private static final String ELASTICSEARCH_TEMPLATE_ITEM_DEFAULT_TEMPLATE_XML = "src/test/resources/REST-examples/elasticsearch-templateItem-defaultTemplate.xml";
	private static final String ELASTICSEARCH_TEMPLATE_ITEM_ONE_TEMPLATE_XML = "src/test/resources/REST-examples/elasticsearch-templateItem-template1.xml";

	@Test
	public void testValidateTemplateItemThatFindsNoDifferences() throws FileNotFoundException, JAXBException {
		TemplateValidator tv = new TemplateValidator();
		TemplateItem templateItem = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_DEFAULT_TEMPLATE_XML, TemplateItem.class);
		TemplateItem templateItem2 = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_DEFAULT_TEMPLATE_XML, TemplateItem.class);
		TemplateValidationResult result = tv.validateTemplateItem(templateItem, templateItem2);
		assertFalse(result.isErrored());
		assertEquals(0,result.getErrors().size());
	}
	
	@Test
	public void testValidateTemplateItemWhereTemplateIdIsUpdated() throws FileNotFoundException, JAXBException {
		TemplateValidator tv = new TemplateValidator();
		TemplateItem templateItem = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_DEFAULT_TEMPLATE_XML, TemplateItem.class);
		TemplateItem templateItem2 = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_DEFAULT_TEMPLATE_XML, TemplateItem.class);
		templateItem2.setId("someOtherId");
		TemplateValidationResult result = tv.validateTemplateItem(templateItem, templateItem2);
		assertTrue(result.isErrored());
		assertEquals(1,result.getErrors().size());
		assertTrue(result.getErrors().get("id").contains("The id field must match the existing one"));
	}
	
	@Test
	public void testValidateTemplateItemWhereReadOnlyBuildStartedIsUpdated() throws FileNotFoundException, JAXBException {
		TemplateValidator tv = new TemplateValidator();
		TemplateItem templateItem = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_DEFAULT_TEMPLATE_XML, TemplateItem.class);
		TemplateItem templateItem2 = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_DEFAULT_TEMPLATE_XML, TemplateItem.class);
		templateItem2.findConfigForBuildState("buildStarted").setEnabled(false);
		TemplateValidationResult result = tv.validateTemplateItem(templateItem, templateItem2);
		assertTrue(result.isErrored());
		assertEquals(1,result.getErrors().size());
		assertTrue(result.getErrors().get("buildStarted").contains("buildStarted is an not editable"));
	}
	
	@Test
	public void testValidateTemplateItemWhereBuildStartedIsRenamedToInvaliateBuildStateShortName() throws FileNotFoundException, JAXBException {
		TemplateValidator tv = new TemplateValidator();
		TemplateItem templateItem = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_DEFAULT_TEMPLATE_XML, TemplateItem.class);
		TemplateItem templateItem2 = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_DEFAULT_TEMPLATE_XML, TemplateItem.class);
		templateItem2.findConfigForBuildState("buildStarted").setType("invalidname");
		TemplateValidationResult result = tv.validateTemplateItem(templateItem, templateItem2);
		assertTrue(result.isErrored());
		assertEquals(1,result.getErrors().size());
		assertTrue(result.getErrors().get("invalidname").contains("invalidname is an not a valid buildState"));
	}
	
	@Test
	public void testValidateTemplateItem1ThatFindsNoDifferences() throws FileNotFoundException, JAXBException {
		TemplateValidator tv = new TemplateValidator();
		TemplateItem templateItem = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_ONE_TEMPLATE_XML, TemplateItem.class);
		TemplateItem templateItem2 = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_ONE_TEMPLATE_XML, TemplateItem.class);
		TemplateValidationResult result = tv.validateTemplateItem(templateItem, templateItem2);
		assertFalse(result.isErrored());
		assertEquals(0,result.getErrors().size());
	}
	
	@Test
	public void testValidateTemplateItem1WhereTemplateIdIsUpdated() throws FileNotFoundException, JAXBException {
		TemplateValidator tv = new TemplateValidator();
		TemplateItem templateItem = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_ONE_TEMPLATE_XML, TemplateItem.class);
		TemplateItem templateItem2 = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_ONE_TEMPLATE_XML, TemplateItem.class);
		templateItem2.setId("someOtherId");
		TemplateValidationResult result = tv.validateTemplateItem(templateItem, templateItem2);
		assertTrue(result.isErrored());
		assertEquals(1,result.getErrors().size());
		assertTrue(result.getErrors().get("id").contains("The id field must match the existing one"));
	}
	
	@Test
	public void testValidateTemplateItem1WhereBuildStartedIsUpdated() throws FileNotFoundException, JAXBException {
		TemplateValidator tv = new TemplateValidator();
		TemplateItem templateItem = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_ONE_TEMPLATE_XML, TemplateItem.class);
		TemplateItem templateItem2 = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_ONE_TEMPLATE_XML, TemplateItem.class);
		templateItem2.findConfigForBuildState("buildStarted").setEnabled(false);
		TemplateValidationResult result = tv.validateTemplateItem(templateItem, templateItem2);
		assertFalse(result.isErrored());
		assertEquals(0,result.getErrors().size());
	}
	
	@Test
	public void testValidateTemplateItem1WhereBuildStartedIsRenamedToInvaliateBuildStateShortName() throws FileNotFoundException, JAXBException {
		TemplateValidator tv = new TemplateValidator();
		TemplateItem templateItem = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_ONE_TEMPLATE_XML, TemplateItem.class);
		TemplateItem templateItem2 = new JaxHelper<TemplateItem>().read(ELASTICSEARCH_TEMPLATE_ITEM_ONE_TEMPLATE_XML, TemplateItem.class);
		templateItem2.findConfigForBuildState("buildStarted").setType("invalidname");
		TemplateValidationResult result = tv.validateTemplateItem(templateItem, templateItem2);
		assertTrue(result.isErrored());
		assertEquals(1,result.getErrors().size());
		assertTrue(result.getErrors().get("invalidname").contains("invalidname is an not a valid buildState"));
	}

}
