package webhook.teamcity.payload.util;

public class StringUtils {
	
    public static String stripTrailingSlash(String stringWithPossibleTrailingSlash){
    	if (stringWithPossibleTrailingSlash.endsWith("/")){
    		return stringWithPossibleTrailingSlash.substring(0, stringWithPossibleTrailingSlash.length()-1);
    	}
    	return stringWithPossibleTrailingSlash;
    	
    }

}
