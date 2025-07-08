package webhook.teamcity.settings.converter;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jdom.JDOMException;
import org.junit.Test;
import org.mockito.Mock;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import webhook.teamcity.settings.entity.ProjectFeatureEntity;
import webhook.teamcity.settings.entity.ProjectFeatureEntity.ProjectFeatureExtension;
import webhook.testframework.util.ConfigLoaderUtil;


public class ProjectFeatureSerialiserTest {
    
    @Mock
    ProjectManager projectManager;
    private List<SProjectFeatureDescriptor> webhooksAsProjectFeatures;
    
    @Test
    public void ProjectFeaturesToXmlTest() throws JDOMException, IOException, JAXBException {
        webhooksAsProjectFeatures = ConfigLoaderUtil.getListOfProjectFeatures(new File("src/test/resources/testMigrationConfigurations/projects/FirstProject/project-config.xml"));
        ProjectFeatureEntity e = new ProjectFeatureEntity();
        e.setProjectExtensions(webhooksAsProjectFeatures.stream().map(
            f -> ProjectFeatureExtension.init(f.getId(), f.getType(), f.getParameters())
            ).collect(Collectors.toList()));
        
        JAXBContext context = JAXBContext.newInstance(ProjectFeatureEntity.class);
        Marshaller m = context.createMarshaller();
        StringWriter sw = new StringWriter();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        m.marshal(e, sw);
        System.out.print(sw.toString());
        assertTrue(webhooksAsProjectFeatures.size() > 0);
    }

}
