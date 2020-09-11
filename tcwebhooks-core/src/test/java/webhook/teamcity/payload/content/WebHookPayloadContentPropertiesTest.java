package webhook.teamcity.payload.content;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class WebHookPayloadContentPropertiesTest {
	
	ExtraParameters  extraParameters  = new ExtraParameters(); 
	SortedMap<String, String>  templates  = new TreeMap<String, String>(); 
	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);

	@Test
	public void test() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		templates.put("buildStatusHtml", "dlkjdlkf");
		MockitoAnnotations.initMocks(this);
		WebHookPayloadContent content = new WebHookPayloadContent(
				framework.getWebHookVariableResolverManager().getVariableResolverFactory(PayloadTemplateEngineType.STANDARD), framework.getServer(), framework.getRunningBuild(), 
				framework.getPreviousSuccessfulBuild(), BuildStateEnum.BUILD_FINISHED, 
				extraParameters, 
				templates) {
		};
		Map<String,String> props = BeanUtils.describe(content);
		for (Entry<String, String> entry : props.entrySet()){
			System.out.print("\"" + entry.getKey() + "\", ");
		}
	}

}
