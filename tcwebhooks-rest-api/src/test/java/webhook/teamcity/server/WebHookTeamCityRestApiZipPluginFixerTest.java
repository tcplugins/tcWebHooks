package webhook.teamcity.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class WebHookTeamCityRestApiZipPluginFixerTest {

	@Test
	public void testFindRestApiZipFileInTomcatDir() throws IOException {
		WebHookTeamCityRestApiZipPluginFixer fixer = new WebHookTeamCityRestApiZipPluginFixer();
		List<Path> filesFound = fixer.findRestApiZipFileInTomcatDir(new File("src/test/resources/catalina_home"), "somefile.zip");
		assertEquals(1, filesFound.size());
		assertEquals("src/test/resources/catalina_home/webapps/ROOT/WEB-INF/plugins/somefile.zip", filesFound.get(0).toString());
	}

	@Test
	public void testDoesRestApiZipFileContainJaxJars() throws IOException {
		WebHookTeamCityRestApiZipPluginFixer fixer = new WebHookTeamCityRestApiZipPluginFixer();
		List<Path> filesFound = fixer.findRestApiZipFileInTomcatDir(new File("src/test/resources/catalina_home"), "rest-api.zip");
		assertEquals(1, filesFound.size());
		boolean fileContainsJars = fixer.doesRestApiZipFileContainJaxJars(filesFound.get(0).toFile(), WebHookTeamCityRestApiZipPluginFixer.filenames);
		assertTrue(fileContainsJars);
	}
	
	@Test
	public void testDoesRestApiZipFileNotContainJaxJars() throws IOException {
		WebHookTeamCityRestApiZipPluginFixer fixer = new WebHookTeamCityRestApiZipPluginFixer();
		List<Path> filesFound = fixer.findRestApiZipFileInTomcatDir(new File("src/test/resources/catalina_home"), "somefile.zip");
		assertEquals(1, filesFound.size());
		boolean fileContainsJars = fixer.doesRestApiZipFileContainJaxJars(filesFound.get(0).toFile(), WebHookTeamCityRestApiZipPluginFixer.filenames);
		assertFalse(fileContainsJars);
	}

	@Test
	public void testDeleteFilesFromRestApiZipFile() throws IOException {
		
		FileUtils.copyDirectoryToDirectory(new File("src/test/resources/catalina_home"), new File("target"));
		WebHookTeamCityRestApiZipPluginFixer fixer = new WebHookTeamCityRestApiZipPluginFixer();
		List<Path> filesFound = fixer.findRestApiZipFileInTomcatDir(new File("target/catalina_home"), "rest-api.zip");
		assertEquals(1, filesFound.size());
		assertTrue(fixer.doesRestApiZipFileContainJaxJars(filesFound.get(0).toFile(), WebHookTeamCityRestApiZipPluginFixer.filenames));
		fixer.deleteFilesFromRestApiZipFile(filesFound.get(0).toFile(), WebHookTeamCityRestApiZipPluginFixer.filenames);
		assertFalse(fixer.doesRestApiZipFileContainJaxJars(filesFound.get(0).toFile(), WebHookTeamCityRestApiZipPluginFixer.filenames));
		
	}
	
	@Test
	public void testFindTeamCityBaseLocation() {
		WebHookTeamCityRestApiZipPluginFixer fixer = new WebHookTeamCityRestApiZipPluginFixer();
		fixer.findTeamCityBaseLocation();
	}

}
