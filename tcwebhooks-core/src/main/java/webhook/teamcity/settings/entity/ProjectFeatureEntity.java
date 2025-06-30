package webhook.teamcity.settings.entity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * <project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" parent-id="RootParentProjectId" uuid="" xsi:noNamespaceSchemaLocation="https://www.jetbrains.com/teamcity/schemas/2021.1/project-config.xsd">
  <name>RootProjectName</name>
  <description>Small Kotlin based project from VCS</description>
  <parameters />
  <project-extensions>
    <extension id="PROJECT_EXT_1" type="tcWebHooks">
      <parameters>
        <param name="authentication" value="basicAuth" />
        <param name="basicAuthPassword" value="myPassword" />
        <param name="basicAuthPreemptive" value="true" />
        <param name="basicAuthRealm" value="myRealm" />
        <param name="basicAuthUsername" value="myUserName" />
        <param name="buildAddedToQueue" value="enabled" />
        <param name="buildRemovedFromQueue" value="enabled" />
        <param name="buildStates" value="enabledBuildStates" />
        <param name="buildTypeIds" value="MyProjectId_TcDummyDeb, MyProjectId_TcWebHooks" />
        <param name="buildTypes" value="selectedProjectBuilds" />
        <param name="subProjectBuilds" value="true" />
        <param name="template" value="legacy-json" />
        <param name="url" value="http://localhost:8111/webhooks/endpoint.html?vcs_test=1" />
        <param name="webHookId" value="MyProjectId_WebHook_01" />
      </parameters>
    </extension>
    </project-extensions>
    </<project>
 */

@Data @NoArgsConstructor
@XmlRootElement(name = "project")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectFeatureEntity {
    
    @XmlElementWrapper(name="project-extensions") @XmlElement(name="extension")
    List<ProjectFeatureExtension> projectExtensions;

    @Data @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ProjectFeatureExtension {
        @XmlAttribute
        String id;
        @XmlAttribute
        String type;
        
        @XmlElementWrapper(name="parameters") @XmlElement(name="parameter")
        List<ProjectFeatureParameter> parameters ;
        
        public static ProjectFeatureExtension init(String id, String type, Map<String,String> parameters) {
            ProjectFeatureExtension ext = new ProjectFeatureExtension();
            ext.setId(id);
            ext.setType(type);
            List<ProjectFeatureParameter> p = parameters.entrySet().stream().map(e -> new ProjectFeatureParameter(e.getKey(),  e.getValue())).collect(Collectors.toList());
            ext.setParameters(p);
            return ext;
        }
    }
    
    @Data @NoArgsConstructor @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ProjectFeatureParameter {
        @XmlAttribute
        String name;
        @XmlAttribute
        String value;
    }
}
