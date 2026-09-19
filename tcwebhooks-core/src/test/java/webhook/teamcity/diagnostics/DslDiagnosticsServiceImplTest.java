package webhook.teamcity.diagnostics;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import webhook.teamcity.exception.DslDiagnotisticsException;

public class DslDiagnosticsServiceImplTest {
	
	private static final String EXAMPLE_POM = "src/test/resources/dsl-plugins-repository/org/jetbrains/teamcity/"
			+ "configs-dsl-kotlin-plugins-latest/1.0-SNAPSHOT/configs-dsl-kotlin-plugins-latest-1.0-20260811.232606-1.pom";
	private static final String EXAMPLE_METADATA = "src/test/resources/dsl-plugins-repository/org/jetbrains/teamcity/"
			+ "configs-dsl-kotlin-plugins-latest/1.0-SNAPSHOT/maven-metadata.xml";
	
	@Test
	public void testLoadPom() throws FileNotFoundException, JAXBException, XMLStreamException, FactoryConfigurationError {
		MavenPom pom = new JaxHelper<MavenPom>().read(EXAMPLE_POM, MavenPom.class);
		assertEquals("org.jetbrains.teamcity", pom.getGroupId());
		assertEquals(49, pom.getDependencies().size());
	}
	
	@Test
	public void testLoadMetadata() throws FileNotFoundException, JAXBException, XMLStreamException, FactoryConfigurationError {
		MavenMetaData metaData = new JaxHelper<MavenMetaData>().read(EXAMPLE_METADATA, MavenMetaData.class);
		assertEquals("org.jetbrains.teamcity", metaData.getGroupId());
		assertEquals(1, metaData.getVersioning().getSnapshotVersions().size());
	}

	@Test
	public void testGetTeamCityPom() throws DslDiagnotisticsException {
		DslDiagnosticsService service = new DslDiagnosticsServiceImpl(new TestMavenFileResolver());
		Pair<String, MavenPom> pom = service.getPom(DslDiagnosticsService.PLUGINS_ARTIFACT_ID);
		assertEquals(DslDiagnosticsService.PLUGINS_ARTIFACT_ID, pom.getRight().getArtifactId());
		assertEquals(DslDiagnosticsService.TEAMCITY_GROUP_ID, pom.getRight().getGroupId());
		assertEquals(49, pom.getRight().getDependencies().size());
	}
	
	@Test
	public void testGetTcWebHooksPom() throws DslDiagnotisticsException {
		DslDiagnosticsService service = new DslDiagnosticsServiceImpl(new TestMavenFileResolver());
		Pair<String, MavenPom> pom = service.getPom(DslDiagnosticsService.TCWEBHOOKS_ARTIFACT_ID);
		assertEquals(DslDiagnosticsService.TCWEBHOOKS_ARTIFACT_ID, pom.getRight().getArtifactId());
		assertEquals(DslDiagnosticsService.TEAMCITY_GROUP_ID, pom.getRight().getGroupId());
		assertEquals(1, pom.getRight().getDependencies().size());
	}
	
	@Test
	public void getResult() throws DslDiagnotisticsException {
		DslDiagnosticsService service = new DslDiagnosticsServiceImpl(new TestMavenFileResolver());
		DslDiagnosticsResult result = service.getDiagnostics();
		System.out.println(result);
	}
	
	public static class TestMavenFileResolver implements MavenFileResolver {
		
		@Override
		public MavenMetaData getMetaData(String groupId, String artifactId, String version) {
			try {
				return new JaxHelper<MavenMetaData>().read(
						String.format("src/test/resources/dsl-plugins-repository/%s/%s/%s/maven-metadata.xml", 
								groupId.replace(".", "/"), artifactId, version),
						MavenMetaData.class);
			} catch (Exception e) {
				return null;
			}
		}
		
		@Override
		public MavenPom getPom(String groupId, String artifactId, String version, String snapshotVersion) {
			try {
				return new JaxHelper<MavenPom>().read(
						String.format("src/test/resources/dsl-plugins-repository/%s/%s/%s/%s-%s.pom", 
								groupId.replace(".", "/"), artifactId, version, artifactId, snapshotVersion), MavenPom.class);
			} catch (Exception e) {
				return null;
			}
		}
	}
}
