package webhook.teamcity.payload.content;

import jetbrains.buildServer.serverSide.comments.Comment;

public class WebHooksComment {
	
	String name = "";
	String comment = "";
	
	public static WebHooksComment build (Comment comment){
		if (comment != null){
			WebHooksComment buildComment = new WebHooksComment();
			if (comment.getUser() != null){
				buildComment.name = comment.getUser().getDescriptiveName();
			}
			buildComment.comment = comment.getComment();
			return buildComment;
		}
		return null;
	}
	
	public String getName() {
		return name;
	}
	
	public String getComment() {
		return comment;
	}

	@Override
	public String toString() {
		return name + ": " + comment;
	}
}
