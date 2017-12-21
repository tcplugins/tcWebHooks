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

import lombok.Getter;
import webhook.teamcity.Loggers;

public class WebHookTeamCityRestApiZipPluginFixer {
	
	private final static String[] filenames = { "server/jaxb-api-2.2.5.jar", "server/jaxb-impl-2.2.5.jar"};
	private final static String unpackedLocation = File.separator + ".unpacked" + File.separator + "rest-api";
	
	@Getter
	private boolean haveFilesBeenCleanedSinceBoot = false;
	
	@Getter
	private List<Path> foundApiZipFiles = new ArrayList<>();
	
	@Getter
	private List<Path> foundApiZipFilesContainingJaxbJars = new ArrayList<>();
	
	@Getter
	private List<Path> foundApiZipFilesNotContainingJaxbJars = new ArrayList<>();
	
	@Getter
	private List<Path> foundUnpackedApiZipFilesContainingJaxbJars = new ArrayList<>();
	
	@Getter
	private List<Path> foundUnpackedApiZipFilesNotContainingJaxbJars = new ArrayList<>();
	
	public boolean foundApiZipFilesContainingJaxbJars() {
		return foundApiZipFilesContainingJaxbJars.size() > 0 || foundUnpackedApiZipFilesContainingJaxbJars.size() > 0;
	}
	
	protected static String[] getFilenames () {
		return filenames;
	}
	
	public synchronized void findRestApiZipPlugins() {
		Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Starting to check if rest-api.zip has jaxb jars");
		File possibleLocation = findTeamCityBaseLocation();
		this.foundApiZipFiles = new ArrayList<>();
		this.foundApiZipFilesContainingJaxbJars = new ArrayList<>();
		this.foundApiZipFilesNotContainingJaxbJars = new ArrayList<>();
		this.foundUnpackedApiZipFilesContainingJaxbJars  = new ArrayList<>();
		this.foundUnpackedApiZipFilesNotContainingJaxbJars = new ArrayList<>();
		
		if (possibleLocation != null) {
			Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Looking for teamcity in: " + possibleLocation.getAbsolutePath());
			try {
				foundApiZipFiles.addAll(findRestApiZipFileInTomcatDir(possibleLocation, "rest-api.zip")); 
				
				for (Path p : foundApiZipFiles){
					if (doesRestApiZipFileContainJaxJars(p.toFile(), filenames)) {
						Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: File found does contain jars: " + p.toFile().getAbsolutePath());
						foundApiZipFilesContainingJaxbJars.add(p);
					} else {
						Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Hooray! File found does not contain jars. Searched in: " + p.toFile().getAbsolutePath());
						foundApiZipFilesNotContainingJaxbJars.add(p);
					}
					String restApiUnpackedDir = p.toFile().getParent() + unpackedLocation;
					Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Looking for unpacked jars in: " + restApiUnpackedDir);
					if (doFilesExistInPluginsUnpackedDir(p.toFile().getParent(), filenames)) {
						Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Unpacked dir does contain jars: " + restApiUnpackedDir);
						foundUnpackedApiZipFilesContainingJaxbJars.add(p);
					} else {
						Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Hooray! Unpacked dir does not contain jars. Searched in: " + restApiUnpackedDir);
						foundUnpackedApiZipFilesNotContainingJaxbJars.add(p);
					}
				}
			} catch (IOException e) {
				Loggers.SERVER.warnAndDebugDetails("WebHookTeamCityRestApiZipPluginFixer :: Could not open zip file rest-api.zip", e);
			}
		} else {
			Loggers.SERVER.warn("WebHookTeamCityRestApiZipPluginFixer :: Unable to determine teamcity install location. No attempt will be made to fix rest-api.zip");
		}
	}
	
	public synchronized void fixRestApiZipPlugin(Path p) {
		try {
				if (doesRestApiZipFileContainJaxJars(p.toFile(), filenames)) {
					Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: File found does contain jars. Attempting to remove them from: " + p.toFile().getAbsolutePath());
					deleteFilesFromRestApiZipFile(p.toFile(), filenames);
					if (doesRestApiZipFileContainJaxJars(p.toFile(), filenames)) {
						Loggers.SERVER.warn("WebHookTeamCityRestApiZipPluginFixer :: File found does contain jars. It was not possible to remove them from: " + p.toFile().getAbsolutePath());
					} else {
						Loggers.SERVER.info("WebHookTeamCityRestApiZipPluginFixer :: Successfully removed jaxb jars from: " + p.toFile().getAbsolutePath());
						Loggers.SERVER.info("WebHookTeamCityRestApiZipPluginFixer :: Please restart TeamCity so that the updated plugin file is loaded.");
						this.haveFilesBeenCleanedSinceBoot = true;
					}
				} else {
					Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Hooray! File found does not contain jars. Searched in: " + p.toFile().getAbsolutePath());
				}
				String restApiUnpackedDir = p.toFile().getParent() + unpackedLocation;
				Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Looking for unpacked jars in: " + restApiUnpackedDir);
				if (doFilesExistInPluginsUnpackedDir(p.toFile().getParent(), filenames)) {
					Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Unpacked dir does contain jars. Attempting to remove them from: " + restApiUnpackedDir);
					deleteFilesFromPluginsUnpackedDir(p.toFile().getParent(), filenames);
					if (doFilesExistInPluginsUnpackedDir(p.toFile().getParent(), filenames)) {
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
	}
	
	protected File findTeamCityBaseLocation() {
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
        for (String envName : env.keySet()) {
        	if ("CATALINA_HOME".equals(envName)){
        		File catalinaHome = new File(env.get(envName));
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
	
	protected boolean doesRestApiZipFileContainJaxJars(File restApiZip, String[] filenames) throws IOException {
        /* Define ZIP File System Properies in HashMap */    
        Map<String, String> zip_properties = new HashMap<>(); 
        /* We want to read an existing ZIP File, so we set this to False */
        zip_properties.put("create", "false"); 

        /* Specify the path to the ZIP File that you want to read as a File System */
        URI zip_disk = null;
        try {
        	Path path = Paths.get(restApiZip.getAbsolutePath());
        	zip_disk = new URI("jar",path.toUri().toString(), null);
    	} catch(URISyntaxException e) {
    		
    	}
        boolean fileFoundInZip = false;
        
        /* Create ZIP file System */
        try (FileSystem zipfs = FileSystems.newFileSystem(zip_disk, zip_properties)) {
            /* Get the Path inside ZIP File*/
        	for (String filename : filenames) {
        		Path pathInZipfile = zipfs.getPath(filename);
        		if (Files.exists(pathInZipfile)) {
        			fileFoundInZip = true;
        		}
        	}
        } catch(IllegalArgumentException e) {
        	Loggers.SERVER.error("WebHookTeamCityRestApiZipPluginFixer :: Could not create filesystem: " + e.getMessage());
        	Loggers.SERVER.debug(e);
        }
        return fileFoundInZip;
	}
	
	protected boolean doFilesExistInPluginsUnpackedDir(String pluginsDir, String[] filenames) throws IOException {
		
		File restPluginUnpackedDir = new File(pluginsDir + unpackedLocation);
		
		boolean fileExists = false;
		
		if (restPluginUnpackedDir.exists() && restPluginUnpackedDir.isDirectory()) {
			for (String filename : filenames) {
				File jaxbjar = new File(restPluginUnpackedDir + File.separator + filename.replace("/", File.separator));
				if (jaxbjar.exists()) {
					fileExists = true;
				}
			}
		} else {
			Loggers.SERVER.debug("WebHookTeamCityRestApiZipPluginFixer :: Unpacked dir does not exist in: " + restPluginUnpackedDir);
		}
		
		return fileExists;
	}
	
	protected boolean deleteFilesFromPluginsUnpackedDir(String pluginsDir, String[] filenames) throws IOException {
		
		File restPluginUnpackedDir = new File(pluginsDir + unpackedLocation);
		
		boolean fileDeletedFromDir = false;
		
		if (restPluginUnpackedDir.exists() && restPluginUnpackedDir.isDirectory()) {
			for (String filename : filenames) {
				File jaxbjar = new File(restPluginUnpackedDir + File.separator + filename.replace("/", File.separator));
				if (jaxbjar.exists()) {
					if (jaxbjar.delete()) {
						fileDeletedFromDir = true;
					}
				}
			}
		}
		return fileDeletedFromDir;
	}
	
	protected boolean deleteFilesFromRestApiZipFile(File restApiZip, String[] filenames) throws IOException {
		/* Define ZIP File System Properies in HashMap */    
		Map<String, String> zip_properties = new HashMap<>(); 
		/* We want to read an existing ZIP File, so we set this to False */
		zip_properties.put("create", "false"); 
		
		/* Specify the path to the ZIP File that you want to read as a File System */
		//URI zip_disk = URI.create("jar:file:" + restApiZip.getAbsolutePath().replace("\\", "/"));
		URI zip_disk = null;
        try {
        	Path path = Paths.get(restApiZip.getAbsolutePath());
        	zip_disk = new URI("jar",path.toUri().toString().replace("\\","/"), null);
    	} catch(URISyntaxException e) {
    		
    	}
		boolean fileDeletedInZip = false;
		
		/* Create ZIP file System */
		try (FileSystem zipfs = FileSystems.newFileSystem(zip_disk, zip_properties)) {
			/* Get the Path inside ZIP File*/
			for (String filename : filenames) {
				Path pathInZipfile = zipfs.getPath(filename);
				if (Files.exists(pathInZipfile)) {
					Files.delete(pathInZipfile);
					fileDeletedInZip = true;
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
        System.err.println(exc);
        return CONTINUE;
    }
}
}
