package webhook.teamcity.payload.util;

public class StringSanitiser {

	public static String sanitise(String dirtyString) {
		return dirtyString
				.replace("<", "_")
				.replace(">", "_")
				.replace("\\", "_")
				.replace("/", "_")
				.replace("$", "_")
				.replace("%", "_")
				.replace("#", "_")
				.replace("@", "_")
				.replace("!", "_")
				.replace("`", "_")
				.replace("~", "_")
				.replace("?", "_")
				.replace("|", "_")
				.replace("*", "_")
				.replace("(", "_")
				.replace(")", "_")
				.replace("^", "_")
				;
	}
	
	public static String sanitize(String dirtyString){
		return sanitise(dirtyString);
	}
 
}
