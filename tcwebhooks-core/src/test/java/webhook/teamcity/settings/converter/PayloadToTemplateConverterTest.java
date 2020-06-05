package webhook.teamcity.settings.converter;

import static org.junit.Assert.*;

import org.junit.Test;

import webhook.teamcity.payload.format.WebHookPayloadEmpty;
import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.payload.format.WebHookPayloadNameValuePairs;
import webhook.teamcity.payload.format.WebHookPayloadTailoredJson;
import webhook.teamcity.payload.format.WebHookPayloadXml;
import webhook.teamcity.payload.template.LegacyEmptyWebHookTemplate;
import webhook.teamcity.payload.template.LegacyJsonWebHookTemplate;
import webhook.teamcity.payload.template.LegacyNameValuePairsWebHookTemplate;
import webhook.teamcity.payload.template.LegacyTailoredJsonWebHookTemplate;
import webhook.teamcity.payload.template.LegacyXmlWebHookTemplate;

public class PayloadToTemplateConverterTest {

	@Test
	public void testTransformNvPairsPayloadToTemplate() {
		String newTemplateId = PayloadToTemplateConverter.transformPayloadToTemplate(
				WebHookPayloadNameValuePairs.FORMAT_SHORT_NAME, 
				null);
		assertEquals(LegacyNameValuePairsWebHookTemplate.TEMPLATE_ID, newTemplateId);
	}
	
	@Test
	public void testTransformNvPairsPayloadToTemplate2() {
		String newTemplateId = PayloadToTemplateConverter.transformPayloadToTemplate(
				WebHookPayloadNameValuePairs.FORMAT_SHORT_NAME, 
				"none");
		assertEquals(LegacyNameValuePairsWebHookTemplate.TEMPLATE_ID, newTemplateId);
	}
	
	@Test
	public void testTransformEmptyPayloadToTemplate() {
		String newTemplateId = PayloadToTemplateConverter.transformPayloadToTemplate(
				WebHookPayloadEmpty.FORMAT_SHORT_NAME, 
				null);
		assertEquals(LegacyEmptyWebHookTemplate.TEMPLATE_ID, newTemplateId);
	}
	@Test
	public void testTransformEmptyPayloadToTemplate2() {
		String newTemplateId = PayloadToTemplateConverter.transformPayloadToTemplate(
				WebHookPayloadEmpty.FORMAT_SHORT_NAME, 
				"none");
		assertEquals(LegacyEmptyWebHookTemplate.TEMPLATE_ID, newTemplateId);
	}
	
	@Test
	public void testTransformJsonPayloadToTemplate() {
		String newTemplateId = PayloadToTemplateConverter.transformPayloadToTemplate(
				WebHookPayloadJson.FORMAT_SHORT_NAME, 
				null);
		assertEquals(LegacyJsonWebHookTemplate.TEMPLATE_ID, newTemplateId);
	}
	@Test
	public void testTransformJsonPayloadToTemplate2() {
		String newTemplateId = PayloadToTemplateConverter.transformPayloadToTemplate(
				WebHookPayloadJson.FORMAT_SHORT_NAME, 
				"none");
		assertEquals(LegacyJsonWebHookTemplate.TEMPLATE_ID, newTemplateId);
	}
	
	@Test
	public void testTransformXmlPayloadToTemplate() {
		String newTemplateId = PayloadToTemplateConverter.transformPayloadToTemplate(
				WebHookPayloadXml.FORMAT_SHORT_NAME, 
				null);
		assertEquals(LegacyXmlWebHookTemplate.TEMPLATE_ID, newTemplateId);
	}
	@Test
	public void testTransformXmlPayloadToTemplate2() {
		String newTemplateId = PayloadToTemplateConverter.transformPayloadToTemplate(
				WebHookPayloadXml.FORMAT_SHORT_NAME, 
				"none");
		assertEquals(LegacyXmlWebHookTemplate.TEMPLATE_ID, newTemplateId);
	}
	
	@Test
	public void testTransformTailoredJsonPayloadToTemplate() {
		String newTemplateId = PayloadToTemplateConverter.transformPayloadToTemplate(
				WebHookPayloadTailoredJson.FORMAT_SHORT_NAME, 
				null);
		assertEquals(LegacyTailoredJsonWebHookTemplate.TEMPLATE_ID, newTemplateId);
	}
	@Test
	public void testTransformTailoredJsonPayloadToTemplate2() {
		String newTemplateId = PayloadToTemplateConverter.transformPayloadToTemplate(
				WebHookPayloadTailoredJson.FORMAT_SHORT_NAME, 
				"none");
		assertEquals(LegacyTailoredJsonWebHookTemplate.TEMPLATE_ID, newTemplateId);
	}

}
