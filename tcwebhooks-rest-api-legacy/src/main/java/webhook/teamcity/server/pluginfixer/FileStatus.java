package webhook.teamcity.server.pluginfixer;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class FileStatus {
	String filename;
	boolean isFound = true;
	boolean isRemoved = false;
	boolean isErrored = false;
	String failureMessage;
	
	FileStatus(String filename) {
		this.filename = filename;
	}
}