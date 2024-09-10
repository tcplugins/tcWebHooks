package webhook.teamcity.settings;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import webhook.testframework.util.ConfigLoaderUtil;

public class ProjectFeatureToWebHookConfigConverterTest {

    @Test
    public void testConvert() throws JDOMException, IOException {
        //String featureId = "PROJECT_EXT_30";
        WebHookConfig webhook = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/plugin-settings-with-lots-of-examples.xml"));
        webhook.setProjectInternalId("project02");
        ProjectFeatureToWebHookConfigConverter converter = new ProjectFeatureToWebHookConfigConverter();
        SProjectFeatureDescriptor features = converter.convert(webhook);
        WebHookConfig convertedWebHook = converter.convert(features);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.print(gson.toJson(webhook));
        //System.out.print(gson.toJson(convertedWebHook));
        assertEquals(gson.toJson(webhook), gson.toJson(convertedWebHook));
        //assertTrue(EqualsBuilder.reflectionEquals(webhook.getAsElement() ,convertedWebHook.getAsElement()));
    }

}
