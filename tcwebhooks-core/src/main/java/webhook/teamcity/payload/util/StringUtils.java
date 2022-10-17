package webhook.teamcity.payload.util;

public class StringUtils {
	
	private StringUtils() {}
	
    public static String stripTrailingSlash(String stringWithPossibleTrailingSlash){
    	if (stringWithPossibleTrailingSlash.endsWith("/")){
    		return stringWithPossibleTrailingSlash.substring(0, stringWithPossibleTrailingSlash.length()-1);
    	}
    	return stringWithPossibleTrailingSlash;
    	
    }

    public static String subString(String s, int startIndex, int endIndex, int minLength) {
    	
    	// If the string is shorter than minLength, don't do anything to it. 
    	if (s.length() <= minLength) {
    		return s;
    	}
    	
    	if (s.length() <= startIndex) {
    		return "";
    	}

    	if (s.length() <= endIndex || endIndex == -1) {
    		return s.substring(startIndex);
    	}
    	
    	return s.substring(startIndex, endIndex);
    	
    }
    
    public static String capitaliseFirstWord(String s) {
    	return org.apache.commons.lang3.StringUtils.capitalize(s);
    }
    
    public static String capitaliseAllWords(String s) {
    	return org.apache.commons.lang3.text.WordUtils.capitalize(s);
    }
}
