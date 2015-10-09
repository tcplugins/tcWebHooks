package webhook.teamcity.payload.content;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.VcsFileModification;


public class WebHooksChange {

	public static WebHooksChange build(SVcsModification modification) {
		WebHooksChange change = new WebHooksChange();
		change.setChangeCount(modification.getChangeCount());
		change.setComment(modification.getDescription());
		for (VcsFileModification fileModification: modification.getChanges()){
			change.files.add(fileModification.getRelativeFileName());
		}
		return change;
	}

	private List<String> files = new ArrayList<String>();
	private int changeCount;
	private String comment;
	
	private void setComment(String description) {
		this.comment = description;
	}
	
	public String getComment() {
		return comment;
	}

	private void setChangeCount(int changeCount) {
		this.changeCount = changeCount;
	}
	
	public int getChangeCount() {
		return changeCount;
	}
	
	public List<String> getFiles() {
		return files;
	}
	
}
