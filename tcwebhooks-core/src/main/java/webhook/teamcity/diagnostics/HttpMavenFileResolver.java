package webhook.teamcity.diagnostics;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import jetbrains.buildServer.serverSide.ServerSettings;
import lombok.AllArgsConstructor;
import webhook.teamcity.Loggers;
import webhook.teamcity.exception.DslDiagnotisticsException;


@AllArgsConstructor
public class HttpMavenFileResolver implements MavenFileResolver {
	
	private final ServerSettings serverSettings;
	private final static String MAVEN_BASE_URL = "/app/dsl-plugins-repository";

	@Override
	public MavenPom getPom(String groupId, String artifactId, String version, String snapshotVersion) throws DslDiagnotisticsException {
		Loggers.SERVER.debug(String.format("Fetching: %s/%s/%s/%s/%s-%s.pom", 
				getMavenRepoUrl(),
				groupId.replace(".", "/"), artifactId, version, artifactId, snapshotVersion));
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpResponse response = httpClient.execute(
					 new HttpGet(
							
							String.format("%s/%s/%s/%s/%s-%s.pom", 
									getMavenRepoUrl(),
									groupId.replace(".", "/"), artifactId, version, artifactId, snapshotVersion)
						
					));
			return new JaxHelper<MavenPom>().read(response.getEntity().getContent(), MavenPom.class);
		} catch (IOException | UnsupportedOperationException | JAXBException | XMLStreamException | FactoryConfigurationError e) {
			throw new DslDiagnotisticsException(e.getMessage(),
					String.format("%s/%s/%s/%s-%s.pom", 
							groupId.replace(".", "/"), artifactId, version, artifactId, snapshotVersion));
		}
	}

	@Override
	public MavenMetaData getMetaData(String groupId, String artifactId, String version) throws DslDiagnotisticsException {
		Loggers.SERVER.debug(String.format("Fetching: %s/%s/%s/%s/maven-metadata.xml", 
									getMavenRepoUrl(),
									groupId.replace(".", "/"), artifactId, version, artifactId) );
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpResponse response = httpClient.execute(
					 new HttpGet(
							String.format("%s/%s/%s/%s/maven-metadata.xml", 
									getMavenRepoUrl(),
									groupId.replace(".", "/"), artifactId, version, artifactId)
						
					));
			return new JaxHelper<MavenMetaData>().read(response.getEntity().getContent(), MavenMetaData.class);
		} catch (IOException | JAXBException | XMLStreamException | FactoryConfigurationError e) {
			throw new DslDiagnotisticsException(e.getMessage(),
					String.format("%s/%s/%s/maven-metadata.xml", 
							groupId.replace(".", "/"), artifactId, version, artifactId));
		}
	}
	
	private String getMavenRepoUrl() {
		return this.serverSettings.getRootUrl() + MAVEN_BASE_URL;
	}

}
