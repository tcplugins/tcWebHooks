package webhook.teamcity.buildrunner;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;

public class WebHookServiceMessagePropertiesProcessor implements PropertiesProcessor {
	public Collection<InvalidProperty> process(Map<String, String> properties) {
		List<InvalidProperty> result = new Vector<InvalidProperty>();
		return result;
	}

}
