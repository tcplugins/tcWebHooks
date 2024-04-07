package webhook.teamcity.server;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jetbrains.buildServer.serverSide.SBuildServer;
import lombok.Getter;
import webhook.teamcity.Loggers;
import webhook.teamcity.server.pluginfixer.JarReport;

public class WebHookTeamCityRestApiZipPluginFixer {
	
	private SBuildServer mySBuildServer;

	public WebHookTeamCityRestApiZipPluginFixer(SBuildServer sBuildServer) {
		mySBuildServer = sBuildServer;
	}
	
	private static final String[] filenames = { "server/jaxb-api-2.2.5.jar", "server/jaxb-impl-2.2.5.jar"};
	public static final String UNPACKED_LOCATION = File.separator + ".unpacked" + File.separator + "rest-api";
	
	@Getter
	private boolean haveFilesBeenCleanedSinceBoot = false;
	
	@Getter
	private List<Path> foundApiZipFiles = new ArrayList<>();
	
	@Getter
	private Map<Path,JarReport> jarReports = new HashMap<>(); 
	
	public List<Path> getFoundApiZipFilesContainingJaxbJars() {
		List<Path> foundPaths = new ArrayList<>();
		for (Entry<Path,JarReport> entry : jarReports.entrySet()) {
			if (entry.getValue().isjarFileFound()) {
				foundPaths.add(entry.getKey());
			}
		}
		return foundPaths;
	}
	
	public boolean foundApiZipFilesContainingJaxbJars() {
		for (JarReport jarReport : jarReports.values()) {
			if (jarReport.isjarFileFound()) {
				return true;
			}
		}
		return false;
	}
	
	protected static String[] getFilenames () {
		return filenames;
	}
	
	public synchronized void findRestApiZipPlugins() {
		Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Starting to check if rest-api.zip has jaxb jars");
		File possibleLocation = findTeamCityBaseLocation();
		this.foundApiZipFiles = new ArrayList<>();
		
		if (possibleLocation != null) {
			Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Looking for teamcity in: " + possibleLocation.getAbsolutePath());
			try {
				foundApiZipFiles.addAll(findRestApiZipFileInTomcatDir(possibleLocation, "rest-api.zip")); 
				
				for (Path p : foundApiZipFiles){
					String restApiUnpackedDir = p.toFile().getParent() + UNPACKED_LOCATION;
					if (! jarReports.containsKey(p)) {
						jarReports.put(p, new JarReport(p, restApiUnpackedDir, filenames));
					}
					if (doesRestApiZipFileContainJaxJars(p.toFile(), filenames, jarReports.get(p))) {
						Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: File found does contain jars: " + p.toFile().getAbsolutePath());
					} else {
						Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Hooray! File found does not contain jars. Searched in: " + p.toFile().getAbsolutePath());
					}
					Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Looking for unpacked jars in: " + restApiUnpackedDir);
					if (doFilesExistInPluginsUnpackedDir(p.toFile().getParent(), filenames, jarReports.get(p))) {
						Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Unpacked dir does contain jars: " + restApiUnpackedDir);
					} else {
						Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Hooray! Unpacked dir does not contain jars. Searched in: " + restApiUnpackedDir);
					}
				}
			} catch (IOException e) {
				Loggers.SERVER.warnAndDebugDetails("WebHookTeamCityRestApiZipPluginFixer :: Could not open zip file rest-api.zip", e);
			}
		} else {
			Loggers.SERVER.warn("WebHookTeamCityRestApiZipPluginFixer :: Unable to determine teamcity install location. No attempt will be made to fix rest-api.zip");
		}
	}
	
	public synchronized JarReport fixRestApiZipPlugin(Path p) {
		try {
				if (doesRestApiZipFileContainJaxJars(p.toFile(), filenames, jarReports.get(p))) {
					Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: File found does contain jars. Attempting to remove them from: " + p.toFile().getAbsolutePath());
					deleteFilesFromRestApiZipFile(p.toFile(), filenames, jarReports.get(p));
					if (doesRestApiZipFileContainJaxJars(p.toFile(), filenames, jarReports.get(p))) {
						Loggers.SERVER.warn("WebHookTeamCityRestApiZipPluginFixer :: File found does contain jars. It was not possible to remove them from: " + p.toFile().getAbsolutePath());
					} else {
						Loggers.SERVER.info("WebHookTeamCityRestApiZipPluginFixer :: Successfully removed jaxb jars from: " + p.toFile().getAbsolutePath());
						Loggers.SERVER.info("WebHookTeamCityRestApiZipPluginFixer :: Please restart TeamCity so that the updated plugin file is loaded.");
						this.haveFilesBeenCleanedSinceBoot = true;
					}
				} else {
					Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Hooray! File found does not contain jars. Searched in: " + p.toFile().getAbsolutePath());
				}
				String restApiUnpackedDir = p.toFile().getParent() + UNPACKED_LOCATION;
				Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Looking for unpacked jars in: " + restApiUnpackedDir);
				if (doFilesExistInPluginsUnpackedDir(p.toFile().getParent(), filenames, jarReports.get(p))) {
					Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Unpacked dir does contain jars. Attempting to remove them from: " + restApiUnpackedDir);
					deleteFilesFromPluginsUnpackedDir(filenames, jarReports.get(p));
					if (doFilesExistInPluginsUnpackedDir(p.toFile().getParent(), filenames, jarReports.get(p))) {
						Loggers.SERVER.warn("WebHookTeamCityRestApiZipPluginFixer :: Unpacked dir still contains jars. It was not possible to remove them from: " + restApiUnpackedDir);
					} else {
						Loggers.SERVER.info("WebHookTeamCityRestApiZipPluginFixer :: Successfully removed jaxb jars from unpacked dir : " + restApiUnpackedDir);
						Loggers.SERVER.info("WebHookTeamCityRestApiZipPluginFixer :: Please restart TeamCity so that the updated plugin file is loaded.");
						this.haveFilesBeenCleanedSinceBoot = true;
					}
				} else {
					Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Hooray! Unpacked dir does not contain jars. Searched in: " + restApiUnpackedDir);
				}
		} catch (IOException e) {
			Loggers.SERVER.warnAndDebugDetails("WebHookTeamCityRestApiZipPluginFixer :: Could not remove files from rest-api.zip", e);
		}
		return jarReports.get(p);
	}
	
	protected File findTeamCityBaseLocation() {
		
		File teamCityHome = new File(this.mySBuildServer.getServerRootPath() + "/WEB-INF");
		if (teamCityHome.exists() && teamCityHome.isDirectory()) {
			Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: TeamCity WEB-INF is: " + teamCityHome.getAbsolutePath());
			return teamCityHome;
		}
		
		Map<String, String> env = System.getenv();
		File catalinaHome = getCatalinaHomeDir(env); 
		if (catalinaHome != null) {
			Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: CATALINA_HOME is: " + catalinaHome.getAbsolutePath());
			return catalinaHome;
		} else {
			for (Entry<String,String> e : env.entrySet()) {
				Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: " + e.getKey() + " : " + e.getValue());
			}
		}
		
		Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: CATALINA_HOME could not be determined. Next attempt: log4j.configuration property path");
		
		String logLocation = System.getProperty("log4j.configuration");
		if (logLocation != null) {
			File tcHomeDir = getTeamCityHomeDir(logLocation.replaceFirst("file\\:", "").replace("../conf/teamcity-server-log4j.xml", "../"));
			if (tcHomeDir != null) {
				Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: TeamCity appears to be in: " + tcHomeDir.getAbsolutePath());
				return tcHomeDir;
			}
		}
		return null;
	}
	
	protected List<Path> findRestApiZipFileInTomcatDir(File tomcatDir, String filepattern) throws IOException {
		
        Finder finder = new Finder(filepattern);
        Files.walkFileTree(tomcatDir.toPath(), finder);
        return finder.getResults();
		
	}
	
	private File getCatalinaHomeDir(Map<String, String> env) {
        for (Entry<String,String> envName : env.entrySet()) {
        	if ("CATALINA_HOME".equals(envName.getKey())){
        		File catalinaHome = new File(envName.getValue());
        		if (catalinaHome.exists() && catalinaHome.isDirectory()) {
        			return catalinaHome;
        		}
        	}
        }
        return null;
	}
	
	private File getTeamCityHomeDir(String filePath) {
		Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Trying to determine if TeamCity is in: " + filePath);
		File path = new File(filePath);
		if (path.exists() && path.isDirectory()) {
			Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Checking dir contains webapps. Looking in: " + path.getAbsolutePath());
			for (File dirname : path.listFiles()) {
				if ("webapps".equalsIgnoreCase(dirname.getName())) {
					return path;
				}
			}
		}
        return null;
	}
	
	protected boolean doesRestApiZipFileContainJaxJars(File restApiZip, String[] filenames, JarReport jarReport) throws IOException {
        // If we have a new TC version the jar conflict is resolved so just return false
		if (mySBuildServer.getServerMajorVersion() >= 21) {
			Loggers.SERVER.debug("TeamCity is 2021.0 or newer. Skipping ZIP file checking for " + restApiZip.getAbsolutePath());
			for (String filename : filenames) {
        		jarReport.setJarFoundInZipFile(filename, false);
			}
			return false;
		}
		/* Define ZIP File System Properies in HashMap */    
        Map<String, String> zipProperties = new HashMap<>(); 
        /* We want to read an existing ZIP File, so we set this to False */
        zipProperties.put("create", "false"); 

        /* Specify the path to the ZIP File that you want to read as a File System */
        URI zipDisk = null;
        try {
        	Path path = Paths.get(restApiZip.getAbsolutePath());
        	zipDisk = new URI("jar",path.toUri().toString(), null);
    	} catch(URISyntaxException e) {
    		Loggers.SERVER.warn("WebHookTeamCityRestApiZipPluginFixer :: Could not convert to JAR URI.", e);
    	}
        boolean fileFoundInZip = false;
        
        /* Create ZIP file System */
        try (FileSystem zipfs = FileSystems.newFileSystem(zipDisk, zipProperties)) {
            /* Get the Path inside ZIP File*/
        	for (String filename : filenames) {
        		Path pathInZipfile = zipfs.getPath(filename);
        		if (Files.exists(pathInZipfile)) {
        			fileFoundInZip = true;
        			jarReport.setJarFoundInZipFile(filename, true);
        		} else {
        			jarReport.setJarFoundInZipFile(filename, false);
        		}
        	}
        } catch(IllegalArgumentException e) {
        	Loggers.SERVER.error("WebHookTeamCityRestApiZipPluginFixer :: Could not create filesystem: " + e.getMessage());
        	Loggers.SERVER.debug(e);
        }
        return fileFoundInZip;
	}
	
	protected boolean doFilesExistInPluginsUnpackedDir(String pluginsDir, String[] filenames, JarReport jarReport) {
		
		File restPluginUnpackedDir = new File(pluginsDir + UNPACKED_LOCATION);
		
		boolean fileExists = false;
		
		if (restPluginUnpackedDir.exists() && restPluginUnpackedDir.isDirectory()) {
			for (String filename : filenames) {
				File jaxbjar = new File(restPluginUnpackedDir + File.separator + filename.replace("/", File.separator));
				if (jaxbjar.exists()) {
					fileExists = true;
					jarReport.setJarFoundInUnpackedLocation(filename, true);
				} else {
					jarReport.setJarFoundInUnpackedLocation(filename, false);
				}
			}
		} else {
			Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Unpacked dir does not exist in: " + restPluginUnpackedDir);
		}
		
		return fileExists;
	}
	
	protected boolean deleteFilesFromPluginsUnpackedDir(String[] filenames, JarReport jarReport) throws IOException {
		
		File restPluginUnpackedDir = new File(jarReport.getApiZipFileUnpackedLocation());
		
		boolean fileDeletedFromDir = false;
		
		if (restPluginUnpackedDir.exists() && restPluginUnpackedDir.isDirectory()) {
			for (String filename : filenames) {
				Path jaxbjar = new File(restPluginUnpackedDir + File.separator + filename.replace("/", File.separator)).toPath();
				if (Files.exists(jaxbjar)) {
					try {
						Files.delete(jaxbjar);
						if (Files.notExists(jaxbjar)) {
							fileDeletedFromDir = true;
							jarReport.setJarAsRemovedFromUnpackedLocation(filename, true);
						}
					} catch (IOException e) {
						jarReport.setUnpackedLocationFailureMessage(filename, e.getMessage());
						Loggers.SERVER.warnAndDebugDetails("WebHookTeamCityRestApiZipPluginFixer :: Failed to delete file from unpacked location: " + e.getMessage(), e);
					}
				}
			}
		}
		return fileDeletedFromDir;
	}
	
	protected boolean deleteFilesFromRestApiZipFile(File restApiZip, String[] filenames, JarReport jarReport) throws IOException {
		/* Define ZIP File System Properies in HashMap */    
		Map<String, String> zipProperties = new HashMap<>(); 
		/* We want to read an existing ZIP File, so we set this to False */
		zipProperties.put("create", "false"); 
		
		/* Specify the path to the ZIP File that you want to read as a File System */
		URI zipDisk = null;
        try {
        	Path path = Paths.get(restApiZip.getAbsolutePath());
        	zipDisk = new URI("jar",path.toUri().toString().replace("\\","/"), null);
    	} catch(URISyntaxException e) {
    		Loggers.SERVER.warn("WebHookTeamCityRestApiZipPluginFixer :: Could not convert to JAR URI.", e);
    	}
		boolean fileDeletedInZip = false;
		
		/* Create ZIP file System */
		try (FileSystem zipfs = FileSystems.newFileSystem(zipDisk, zipProperties)) {
			/* Get the Path inside ZIP File*/
			for (String filename : filenames) {
				Path pathInZipfile = zipfs.getPath(filename);
				if (Files.exists(pathInZipfile)) {
					Files.delete(pathInZipfile);
					fileDeletedInZip = true;
					jarReport.setJarAsRemovedFromZip(filename, true);
				}
			}
		}
		return fileDeletedInZip;
	}

    public static class Finder
    extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;
    private int numMatches = 0;
    
    private List<Path> matchedPaths = new ArrayList<>();

    Finder(String pattern) {
        matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + pattern);
    }

    // Compares the glob pattern against
    // the file or directory name.
    void find(Path file) {
        Path name = file.getFileName();
        if (name != null && matcher.matches(name)) {
        	matchedPaths.add(file);
            numMatches++;
            Loggers.SERVER.debug("Found file: " + file);
        }
    }

    // Prints the total number of
    // matches to standard out.
    List<Path> getResults() {
    	Loggers.SERVER.debug("Matched: "
            + numMatches);
        return matchedPaths;
    }

    // Invoke the pattern matching
    // method on each file.
    @Override
    public FileVisitResult visitFile(Path file,
            BasicFileAttributes attrs) {
        find(file);
        return CONTINUE;
    }

    // Invoke the pattern matching
    // method on each directory.
    @Override
    public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) {
        find(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
            IOException exc) {
        Loggers.SERVER.warn("WebHookTeamCityRestApiZipPluginFixer ::Visiting file failed", exc);
        return CONTINUE;
    	}
    }

}
