package webhook.teamcity.payload.content;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.VcsFileModification;


public class WebHooksChange {

	public static WebHooksChange build(SVcsModification modification) {
		WebHooksChange change = new WebHooksChange();
		change.setComment(modification.getDescription());
		change.setUsername(modification.getUserName());
		change.setVcsRoot(modification.getVcsRoot().getName());
		for (VcsFileModification fileModification: modification.getChanges()){
			change.files.add(fileModification.getRelativeFileName());
		}
		return change;
	}

	private List<String> files = new ArrayList<String>();
	private String comment;
	private String vcsRoot;
	private String username;
	
	private void setVcsRoot(String name) {
		this.vcsRoot = name;
	}
	
	public String getVcsRoot() {
		return vcsRoot;
	}
	 
	private void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	private void setComment(String description) {
		this.comment = description;
	}
	
	public String getComment() {
		return comment;
	}
	
	public List<String> getFiles() {
		return files;
	}
	
}
