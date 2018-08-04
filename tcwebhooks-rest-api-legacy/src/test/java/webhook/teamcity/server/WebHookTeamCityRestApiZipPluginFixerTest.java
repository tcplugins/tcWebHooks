package webhook.teamcity.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.server.pluginfixer.JarReport;

public class WebHookTeamCityRestApiZipPluginFixerTest {
	
	SBuildServer sBuildServer = mock(SBuildServer.class);

	@Test
	public void testFindRestApiZipFileInTeamCityDir() throws IOException {
		when(sBuildServer.getServerRootPath()).thenReturn("src/test/resources/catalina_home/webapps/ROOT");
		WebHookTeamCityRestApiZipPluginFixer fixer = new WebHookTeamCityRestApiZipPluginFixer(sBuildServer);
		List<Path> filesFound = fixer.findRestApiZipFileInTomcatDir(new File("src/test/resources/catalina_home"), "somefile.zip");
		assertEquals(1, filesFound.size());
		String path = "src/test/resources/catalina_home/webapps/ROOT/WEB-INF/plugins/somefile.zip";
		assertTrue(path.equals(filesFound.get(0).toString()) || path.replace("/","\\").equals(filesFound.get(0).toString()));
	}
	@Test
	public void testFindRestApiZipFileInTomcatDir() throws IOException {
		when(sBuildServer.getServerRootPath()).thenReturn("src/test/resources/empty_teamcity_home/webapps/ROOT");
		WebHookTeamCityRestApiZipPluginFixer fixer = new WebHookTeamCityRestApiZipPluginFixer(sBuildServer);
		List<Path> filesFound = fixer.findRestApiZipFileInTomcatDir(new File("src/test/resources/catalina_home"), "somefile.zip");
		assertEquals(1, filesFound.size());
		String path = "src/test/resources/catalina_home/webapps/ROOT/WEB-INF/plugins/somefile.zip";
		assertTrue(path.equals(filesFound.get(0).toString()) || path.replace("/","\\").equals(filesFound.get(0).toString()));
	}

	@Test
	public void testDoesRestApiZipFileContainJaxJars() throws IOException {
		when(sBuildServer.getServerRootPath()).thenReturn("src/test/resources/empty_teamcity_home/webapps/ROOT");
		WebHookTeamCityRestApiZipPluginFixer fixer = new WebHookTeamCityRestApiZipPluginFixer(sBuildServer);
		List<Path> filesFound = fixer.findRestApiZipFileInTomcatDir(new File("src/test/resources/catalina_home"), "rest-api.zip");
		assertEquals(1, filesFound.size());
		String restApiUnpackedDir = filesFound.get(0).toFile().getParent() + WebHookTeamCityRestApiZipPluginFixer.UNPACKED_LOCATION;
		JarReport jarReport = new JarReport(filesFound.get(0), restApiUnpackedDir, WebHookTeamCityRestApiZipPluginFixer.getFilenames());
		boolean fileContainsJars = fixer.doesRestApiZipFileContainJaxJars(filesFound.get(0).toFile(), WebHookTeamCityRestApiZipPluginFixer.getFilenames(), jarReport);
		assertTrue(fileContainsJars);
	}
	
	@Test
	public void testDoesRestApiZipFileNotContainJaxJars() throws IOException {
		when(sBuildServer.getServerRootPath()).thenReturn("src/test/resources/empty_teamcity_home/webapps/ROOT");
		WebHookTeamCityRestApiZipPluginFixer fixer = new WebHookTeamCityRestApiZipPluginFixer(sBuildServer);
		List<Path> filesFound = fixer.findRestApiZipFileInTomcatDir(new File("src/test/resources/catalina_home"), "somefile.zip");
		assertEquals(1, filesFound.size());
		String restApiUnpackedDir = filesFound.get(0).toFile().getParent() + WebHookTeamCityRestApiZipPluginFixer.UNPACKED_LOCATION;
		JarReport jarReport = new JarReport(filesFound.get(0), restApiUnpackedDir, WebHookTeamCityRestApiZipPluginFixer.getFilenames());
		boolean fileContainsJars = fixer.doesRestApiZipFileContainJaxJars(filesFound.get(0).toFile(), WebHookTeamCityRestApiZipPluginFixer.getFilenames(), jarReport);
		assertFalse(fileContainsJars);
	}

	@Test
	public void testDeleteFilesFromRestApiZipFile() throws IOException {
		when(sBuildServer.getServerRootPath()).thenReturn("src/test/resources/empty_teamcity_home/webapps/ROOT");
		FileUtils.copyDirectoryToDirectory(new File("src/test/resources/catalina_home"), new File("target"));
		WebHookTeamCityRestApiZipPluginFixer fixer = new WebHookTeamCityRestApiZipPluginFixer(sBuildServer);
		List<Path> filesFound = fixer.findRestApiZipFileInTomcatDir(new File("target/catalina_home"), "rest-api.zip");
		assertEquals(1, filesFound.size());
		String restApiUnpackedDir = filesFound.get(0).toFile().getParent() + WebHookTeamCityRestApiZipPluginFixer.UNPACKED_LOCATION;
		JarReport jarReport = new JarReport(filesFound.get(0), restApiUnpackedDir, WebHookTeamCityRestApiZipPluginFixer.getFilenames());
		assertTrue(fixer.doesRestApiZipFileContainJaxJars(filesFound.get(0).toFile(), WebHookTeamCityRestApiZipPluginFixer.getFilenames(), jarReport));
		fixer.deleteFilesFromRestApiZipFile(filesFound.get(0).toFile(), WebHookTeamCityRestApiZipPluginFixer.getFilenames(), jarReport);
		assertFalse(fixer.doesRestApiZipFileContainJaxJars(filesFound.get(0).toFile(), WebHookTeamCityRestApiZipPluginFixer.getFilenames(), jarReport));
		
	}
	
	@Test
	public void testFindTeamCityBaseLocation() {
		when(sBuildServer.getServerRootPath()).thenReturn("src/test/resources/empty_teamcity_home/webapps/ROOT");
		WebHookTeamCityRestApiZipPluginFixer fixer = new WebHookTeamCityRestApiZipPluginFixer(sBuildServer);
		fixer.findTeamCityBaseLocation();
	}

}
