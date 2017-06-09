package webhook.teamcity;

public enum BuildStateEnum {
    BUILD_STARTED 			("buildStarted",			"started",					"Build Started"),                
    CHANGES_LOADED 			("changesLoaded",			"loaded changes",			"Changes Loaded"),                
    //BUILD_CHANGED_STATUS	("statusChanged", 			"changed status"),
    BUILD_INTERRUPTED		("buildInterrupted", 		"been interrupted",			"Build Interrupted"),
    BEFORE_BUILD_FINISHED	("beforeBuildFinish", 		"nearly finished",			"Build Almost Completed"),
	BUILD_FINISHED 			("buildFinished", 			"finished",					"Build Finished"),
	BUILD_SUCCESSFUL		("buildSuccessful", 		"completed successfully",   "Build Successful"),
	BUILD_FAILED			("buildFailed", 			"failed", 					"Build Failed"),
	BUILD_FIXED				("buildFixed", 				"been fixed",				"Build Fixed"),
	BUILD_BROKEN			("buildBroken", 			"broken",					"Build Broken"),
	RESPONSIBILITY_CHANGED	("responsibilityChanged",	"changed responsibility",	"Build Responsibility Changed");
    
    private final String shortName;
    private final String descriptionSuffix;
    private final String shortDescription;
    
    private BuildStateEnum(String shortname, String descriptionSuffix, String shortDescription){
    	this.shortName = shortname;
    	this.descriptionSuffix = descriptionSuffix;
    	this.shortDescription = shortDescription;
    }
    
    /**
     * 
     * @return A short name for the Enum. This is used to uniquely identify the BuildStateEnum
     * as a text string.
     * eg, "buildFixed"
     */
    public String getShortName(){
    	return this.shortName;
    }

    /**
     * @return a string that fits into the sentence "build blahblah has..."
     * eg, "been fixed"
     */
	public String getDescriptionSuffix() {
		return this.descriptionSuffix;
	}
	
    /**
     * @return a string that succinctly describes the state.
     * eg, "Build Broken"
     */
	public String getShortDescription() {
		return shortDescription;
	}
	
	/**
	 * Takes a string and tries to find a BuildStateEnum with a shortName that matches it.
	 * @param stateString as a shortname. eg "buildStarted"
	 * @return BuildStateEnum or null.
	 */
	public static BuildStateEnum findBuildState(String stateString){
		for (BuildStateEnum b : BuildStateEnum.values()) {
			if (b.shortName.equalsIgnoreCase(stateString)){
				return b;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return an array of just the build states that are notifiable
	 */
	public static BuildStateEnum[] getNotifyStates(){
		final BuildStateEnum[] notifyStates = {BUILD_STARTED, CHANGES_LOADED, BUILD_INTERRUPTED, BEFORE_BUILD_FINISHED, BUILD_SUCCESSFUL, BUILD_FAILED, BUILD_FIXED, BUILD_BROKEN, RESPONSIBILITY_CHANGED };
		return notifyStates;
	}
}
