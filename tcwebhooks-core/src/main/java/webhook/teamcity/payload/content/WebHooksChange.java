package webhook.teamcity.payload.content;

import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.VcsFileModification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class WebHooksChange {

    public static WebHooksChange build(SVcsModification modification) {
        WebHooksChange change = new WebHooksChange();
        change.setComment(modification.getDescription());
        change.setVcsRoot(modification.getVcsRoot().getName());
        Collection<SUser> committers = modification.getCommitters();
        SUser committer = committers.iterator().next();
        change.setUsername(committer.getUsername());
        for (VcsFileModification fileModification : modification.getChanges()) {
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

    private void setComment(String description) {
        this.comment = description;
    }

    public String getComment() {
        return comment;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getFiles() {
        return files;
    }

}
