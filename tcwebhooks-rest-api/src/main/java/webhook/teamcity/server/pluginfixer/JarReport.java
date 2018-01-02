package webhook.teamcity.server.pluginfixer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class JarReport {
	
	private Path apiZipFileLocation;
	private String apiZipFileUnpackedLocation;
	
	private Map<String,FileStatus> jarsInZipFile = new HashMap<>();
	private Map<String,FileStatus> jarsInUnpackedLocation = new HashMap<>();
	
	public JarReport(Path apiZipFileLocation, String apiZipFileUnpackedLocation, String[] filenames) {
		this.apiZipFileLocation = apiZipFileLocation;
		this.apiZipFileUnpackedLocation = apiZipFileUnpackedLocation;
		for (String filename : filenames) {
			jarsInZipFile.put(filename, new FileStatus(filename));
			jarsInUnpackedLocation.put(filename, new FileStatus(filename));
		}
	}

	public void setJarFoundInZipFile(String filename, boolean isFound) {
		jarsInZipFile.get(filename).setFound(isFound);
	}
	
	public void setJarFoundInUnpackedLocation(String filename, boolean isFound) {
		jarsInUnpackedLocation.get(filename).setFound(isFound);
	}
	
	public void setJarAsRemovedFromZip(String filename, boolean isRemoved) {
		jarsInZipFile.get(filename).setRemoved(isRemoved);
	}
	public void setJarAsRemovedFromUnpackedLocation(String filename, boolean isRemoved) {
		jarsInUnpackedLocation.get(filename).setRemoved(isRemoved);
	}
	
	public boolean isjarFileFound() {
		for (FileStatus jarStatus : jarsInZipFile.values()) {
			if (jarStatus.isFound) {
				return true;
			}
		}
		for (FileStatus jarStatus : jarsInUnpackedLocation.values()) {
			if (jarStatus.isFound) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isRebootRequired() {
		for (FileStatus jarStatus : jarsInZipFile.values()) {
			if (jarStatus.isRemoved) {
				return true;
			}
		}
		for (FileStatus jarStatus : jarsInUnpackedLocation.values()) {
			if (jarStatus.isRemoved) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isErrored() {
		for (FileStatus jarStatus : jarsInZipFile.values()) {
			if (jarStatus.isErrored) {
				return true;
			}
		}
		for (FileStatus jarStatus : jarsInUnpackedLocation.values()) {
			if (jarStatus.isErrored) {
				return true;
			}
		}
		return false;
	}
	
	public List<String> getFailureMessageList() {
		List<String> messages = new ArrayList<>();
		for (FileStatus jarStatus : jarsInZipFile.values()) {
			if (jarStatus.isErrored) {
				messages.add(jarStatus.failureMessage);
			}
		}
		for (FileStatus jarStatus : jarsInUnpackedLocation.values()) {
			if (jarStatus.isErrored) {
				messages.add(jarStatus.failureMessage);
			}
		}
		return messages;
	}
	
	public void setZipFileFailureMessage(String filename, String message) {
		jarsInZipFile.get(filename).setErrored(true);
		jarsInZipFile.get(filename).setFailureMessage(message);
	}
	public void setUnpackedLocationFailureMessage(String filename, String message) {
		jarsInUnpackedLocation.get(filename).setErrored(true);
		jarsInUnpackedLocation.get(filename).setFailureMessage(message);
		
	}
	
	public Collection<FileStatus> getFilesInZip() {
		return jarsInZipFile.values();
	}
	public Collection<FileStatus> getFilesInUnpackedLocation() {
		return jarsInUnpackedLocation.values();
	}
}
