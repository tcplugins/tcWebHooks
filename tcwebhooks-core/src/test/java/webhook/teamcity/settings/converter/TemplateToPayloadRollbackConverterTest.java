package webhook.teamcity.settings.converter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.jdom.JDOMException;
import org.junit.Test;

import webhook.teamcity.settings.WebHookConfig;
import webhook.testframework.util.ConfigLoaderUtil;

public class TemplateToPayloadRollbackConverterTest {
	
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_valid_template() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate("jsontemplate", "slack.com-compact");
		assertEquals("jsontemplate", pair.getLeft());
		assertEquals("slack.com-compact",pair.getRight());
	}
	
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_valid_nvpairs_legacy_format() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate("nvpairs", null);
		assertEquals("nvpairs", pair.getLeft());
		assertNull(pair.getRight());
	}
	
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_valid_json_legacy_format() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate("json", null);
		assertEquals("json", pair.getLeft());
		assertNull(pair.getRight());
	}
	
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_valid_xml_legacy_format() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate("xml", null);
		assertEquals("xml", pair.getLeft());
		assertNull(pair.getRight());
	}
	
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_valid_jsontailored_legacy_format() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate("tailoredjson", null);
		assertEquals("tailoredjson", pair.getLeft());
		assertNull(pair.getRight());
	}
	
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_valid_json_legacy_format_and_none_template() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate("json", "none");
		assertEquals("json", pair.getLeft());
		assertEquals("none", pair.getRight());
	}
	
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_valid_empty_legacy_format_and_none_template() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate("empty", "none");
		assertEquals("empty", pair.getLeft());
		assertEquals("none", pair.getRight());
	}
	
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_new_xml_legacy_format() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate(null, "legacy-xml");
		assertEquals("xml", pair.getLeft());
		assertEquals("none", pair.getRight());
	}
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_new_json_legacy_format() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate(null, "legacy-json");
		assertEquals("json", pair.getLeft());
		assertEquals("none", pair.getRight());
	}
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_new_tailoredjson_legacy_format() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate(null, "legacy-tailored-json");
		assertEquals("tailoredjson", pair.getLeft());
		assertEquals("none", pair.getRight());
	}
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_new_nvpairs_legacy_format() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate(null, "legacy-nvpairs");
		assertEquals("nvpairs", pair.getLeft());
		assertEquals("none", pair.getRight());
	}
	
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_new_empty_legacy_format() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate(null, "legacy-empty");
		assertEquals("empty", pair.getLeft());
		assertEquals("none", pair.getRight());
	}
	
	@Test
	public void testTransformTemplateToPayloadAndTemplate_with_new_template() {
		Pair<String,String> pair = TemplateToPayloadRollbackConverter.transformTemplateToPayloadAndTemplate(null, "slack.com-compact");
		assertEquals("jsonTemplate", pair.getLeft());
		assertEquals("slack.com-compact",pair.getRight());
	}

	
	@Test
	public void testLoadingNvPairsConfigWithValid1_1File() throws JDOMException, IOException {
		WebHookConfig config = ConfigLoaderUtil.getSpecificWebHookInConfig(1, new File("src/test/resources/project-settings-test-1_2-rollback.xml"));
		assertEquals("nvpairs", config.getPayloadFormat());
		assertNull(config.getPayloadTemplate());
	}
	
	@Test
	public void testLoadingJsonConfigWithValid1_1File() throws JDOMException, IOException {
		WebHookConfig config = ConfigLoaderUtil.getSpecificWebHookInConfig(2, new File("src/test/resources/project-settings-test-1_2-rollback.xml"));
		assertEquals("json", config.getPayloadFormat());
		assertNull(config.getPayloadTemplate());
	}
	
	@Test
	public void testLoadingNvpairsConfigWithValid1_2File() throws JDOMException, IOException {
		WebHookConfig config = ConfigLoaderUtil.getSpecificWebHookInConfig(3, new File("src/test/resources/project-settings-test-1_2-rollback.xml"));
		assertEquals("nvpairs", config.getPayloadFormat());
		assertEquals("none", config.getPayloadTemplate());
	}
	
	@Test
	public void testLoadingFlowdockConfigWithValid1_2File() throws JDOMException, IOException {
		WebHookConfig config = ConfigLoaderUtil.getSpecificWebHookInConfig(4, new File("src/test/resources/project-settings-test-1_2-rollback.xml"));
		assertEquals("jsonTemplate", config.getPayloadFormat());
		assertEquals("flowdock", config.getPayloadTemplate());
	}
}
