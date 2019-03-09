package webhook.teamcity.payload.content;

import java.util.Date;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.ResponsibilityEntry.RemoveMethod;
import jetbrains.buildServer.responsibility.ResponsibilityEntry.State;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class WebHookResponsibilityEntry {

	State state;
	String responsibleUser;
	String reporterUser;
	Date timestamp;
	String comment;
	RemoveMethod removeMethod;
	
	public static WebHookResponsibilityEntry build(ResponsibilityEntry re) {
		if (re == null) {
			return null;
		}
		return builder()
			.state(re.getState())
			.responsibleUser(re.getResponsibleUser() != null ? re.getResponsibleUser().getUsername() : null)
			.reporterUser(re.getReporterUser() != null ? re.getReporterUser().getUsername() : null)
			.timestamp(re.getTimestamp())
			.comment(re.getComment())
			.build();
			
	}

}
