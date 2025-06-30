package webhook.teamcity.settings.converter;

import static org.mockito.Mockito.when;

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
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.ProjectAndBuildTypeResolverImpl;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.auth.basic.UsernamePasswordAuthenticatorFactory;
import webhook.teamcity.auth.bearer.BearerAuthenticatorFactory;
import webhook.teamcity.settings.ProjectFeatureToWebHookConfigConverter;
import webhook.teamcity.settings.entity.ProjectFeatureEntity;
import webhook.teamcity.settings.entity.ProjectFeatureEntity.ProjectFeatureExtension;
import webhook.testframework.util.ConfigLoaderUtil;


public class ProjectFeatureSerialiserTest {
    
    @Mock
    ProjectManager projectManager;
    private ProjectAndBuildTypeResolverImpl buildTypeIdResolver;
    private WebHookAuthenticatorProvider authenticatorProvider;
    private List<SProjectFeatureDescriptor> webhooksAsProjectFeatures;
    private ProjectFeatureToWebHookConfigConverter converter;
    
    //@Before
    public void setup() throws JDOMException, IOException {
        MockSBuildType sBuildType = new MockSBuildType("TcDummyDeb", "TcDummyDeb build", "bt1");
        MockSBuildType sBuildType2 = new MockSBuildType("TcWebHooks", "TcWebHooks build", "bt2");
        MockSBuildType sBuildType3 = new MockSBuildType("TcChatBot", "TcChatBot build", "bt3");
        MockSProject myProject = new MockSProject("My Project", "My Example Project", "project01", "RootProjectId", sBuildType);
        myProject.addANewBuildTypeToTheMock(sBuildType2);
        sBuildType.setProject(myProject);
        sBuildType2.setProject(myProject);
        sBuildType3.setProject(myProject);
        when(projectManager.findBuildTypeByExternalId("RootProjectId_TcDummyDeb")).thenReturn(sBuildType);
        when(projectManager.findBuildTypeByExternalId("RootProjectId_TcWebHooks")).thenReturn(sBuildType2);
        when(projectManager.findBuildTypeByExternalId("RootProjectId_TcChatBot")).thenReturn(sBuildType3);
        when(projectManager.findBuildTypeById("bt1")).thenReturn(sBuildType);
        when(projectManager.findBuildTypeById("bt2")).thenReturn(sBuildType2);
        when(projectManager.findBuildTypeById("bt3")).thenReturn(sBuildType3);
        
        buildTypeIdResolver = new ProjectAndBuildTypeResolverImpl(projectManager);
        authenticatorProvider = new WebHookAuthenticatorProvider();
        authenticatorProvider.registerAuthType(new BearerAuthenticatorFactory(authenticatorProvider));
        authenticatorProvider.registerAuthType(new UsernamePasswordAuthenticatorFactory(authenticatorProvider));
        webhooksAsProjectFeatures = ConfigLoaderUtil.getListOfProjectFeatures(new File("src/test/resources/testMigrationConfigurations/projects/FirstProject/project-config.xml"));
        converter = new ProjectFeatureToWebHookConfigConverter(authenticatorProvider, buildTypeIdResolver);
    }
    
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
    }

}
