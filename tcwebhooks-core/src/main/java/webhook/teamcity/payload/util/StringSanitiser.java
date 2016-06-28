package webhook.teamcity.payload.util;

public class StringSanitiser {

	private StringSanitiser(){}

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
 
}
