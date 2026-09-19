package webhook.teamcity.diagnostics;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.Getter;

/*
 *

	<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	  <modelVersion>4.0.0</modelVersion>
	  <groupId>org.jetbrains.teamcity</groupId>
	  <artifactId>configs-dsl-kotlin-plugins-latest</artifactId>
	  <version>1.0-SNAPSHOT</version>
	  <packaging>pom</packaging>
	
	  <dependencies>
	    <dependency>
	      <groupId>org.jetbrains.teamcity</groupId>
	      <artifactId>configs-dsl-kotlin-github-app-latest</artifactId>
	      <version>1.0-SNAPSHOT</version>
	      <scope>compile</scope>
	    </dependency>
	  </dependencies>
	</project>

 */


@XmlRootElement(name = "project")
//@XmlType(name = "project", propOrder = { "modelVersion", "groupId", "artifactId", "projectId", "dependencies"})
public class MavenPom {
	@XmlElement
	@Getter
	String modelVersion;
	
	@XmlElement
	@Getter
	String groupId;
	
	@XmlElement
	@Getter
	String artifactId;
	
	@XmlElement
	@Getter
	String version;

	@XmlAttribute
	String projectId;
	
	@XmlElement(name = "dependency") @XmlElementWrapper(name = "dependencies") @Getter
	List<Dependency> dependencies;
	
	@XmlRootElement(name = "dependency")
	@XmlType(name = "dependency", propOrder = { "groupId", "artifactId", "version", "scope"})
	@Data @XmlAccessorType(XmlAccessType.FIELD)
	public static class Dependency {
		@XmlElement
		@Getter
		String groupId;
		
		@XmlElement
		@Getter
		String artifactId;
		
		@XmlElement
		@Getter
		String version;
		
		@XmlElement
		@Getter
		String scope;
	}
}
