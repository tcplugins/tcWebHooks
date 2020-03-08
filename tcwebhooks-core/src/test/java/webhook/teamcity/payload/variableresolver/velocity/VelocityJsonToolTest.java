package webhook.teamcity.payload.variableresolver.velocity;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class VelocityJsonToolTest {

	@Test
	public void testJsonToStringMap() {
		VelocityJsonTool jsonTool = new VelocityJsonTool();
		Map<String,String> testMap = jsonTool.jsonToStringMap("{ \"test\" : \"A test\" }");
		assertEquals("A test", testMap.get("test"));
	}
	
	@Test
	public void testJsonToStringMapReturnsEmptyMapForInvalidJsonString() {
		VelocityJsonTool jsonTool = new VelocityJsonTool();
		Map<String,String> testMap = jsonTool.jsonToStringMap("This is invalid json text");
		assertTrue(testMap.isEmpty());
	}
	
	@Test
	public void testJsonToMap() {
		VelocityJsonTool jsonTool = new VelocityJsonTool();
		Map<String,String> testMap = jsonTool.jsonToMap("{ \"test\" : \"A test\" }");
		assertEquals("A test", testMap.get("test"));
	}
	
	@Test
	public void testJsonToMapReturnsEmptyMapForInvalidJsonString() {
		VelocityJsonTool jsonTool = new VelocityJsonTool();
		Map<String,String> testMap = jsonTool.jsonToMap("This is invalid json text");
		assertTrue(testMap.isEmpty());
	}
	
}
