package webhook.teamcity;

public enum BuildStateEnum {
    BUILD_ADDED_TO_QUEUE	("buildAddedToQueue",		"been added to the build queue",		"Build Added to Queue", 			"Queued"),                
    BUILD_REMOVED_FROM_QUEUE("buildRemovedFromQueue",	"been removed from the build queue",	"Build Removed from Queue by User", "De-queued"),                
    BUILD_STARTED 			("buildStarted",			"started",								"Build Started", 					"Started"),                
    CHANGES_LOADED 			("changesLoaded",			"loaded changes",						"Changes Loaded", 					"Changes Loaded"),                
    BUILD_INTERRUPTED		("buildInterrupted", 		"been interrupted",						"Build Interrupted", 				"Interrupted"),
    BEFORE_BUILD_FINISHED	("beforeBuildFinish", 		"nearly finished",						"Build Almost Completed", 			"Almost Completed"),
	BUILD_FINISHED 			("buildFinished", 			"finished",								"Build Finished",					"Finished"),
	BUILD_SUCCESSFUL		("buildSuccessful", 		"completed successfully",   			"Build Successful", 				"Finished (Success)"),
	BUILD_FAILED			("buildFailed", 			"failed", 								"Build Failed", 					"Finished (Failed)"),
	BUILD_FIXED				("buildFixed", 				"been fixed",							"Build Fixed",						"Finished (Fixed)"),
	BUILD_BROKEN			("buildBroken", 			"broken",								"Build Broken",						"Finished (Broken)"),
	RESPONSIBILITY_CHANGED	("responsibilityChanged",	"changed responsibility",				"Build Responsibility Changed", 	"Responsibility Changed"),
	BUILD_PINNED			("buildPinned", 			"been pinned",							"Build Pinned",						"Pinned"), 
	BUILD_UNPINNED			("buildUnpinned",			"been unpinned",						"Build Unpinned",					"Unpinned");
    
    private final String shortName;
    private final String descriptionSuffix;
    private final String shortDescription;
    private final String buildStatusDescription;
    
    private BuildStateEnum(String shortname, String descriptionSuffix, String shortDescription, String buildStatusDescription){
    	this.shortName = shortname;
    	this.descriptionSuffix = descriptionSuffix;
    	this.shortDescription = shortDescription;
    	this.buildStatusDescription = buildStatusDescription;
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
	 * @return a string that summarises the status  in one word.
	 * eg, "Pinned"
	 */
	public String getBuildStatusDescription() {
		return buildStatusDescription;
	}
	
	/**
	 * Takes a string and tries to find a BuildStateEnum with a shortName that matches it.
	 * @param stateString as a shortname. eg "buildStarted"
	 * @return BuildStateEnum or null.
	 */
	public static BuildStateEnum findBuildState(String stateString){
		for (BuildStateEnum buildState : BuildStateEnum.values()) {
			if (buildState.shortName.equalsIgnoreCase(stateString)){
				return buildState;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return an array of just the build states that are notifiable
	 */
	public static BuildStateEnum[] getNotifyStates(){
		return new BuildStateEnum[] {
				BUILD_ADDED_TO_QUEUE, BUILD_REMOVED_FROM_QUEUE, BUILD_STARTED, 
				CHANGES_LOADED, BUILD_INTERRUPTED, BEFORE_BUILD_FINISHED, BUILD_SUCCESSFUL, 
				BUILD_FAILED, BUILD_FIXED, BUILD_BROKEN, RESPONSIBILITY_CHANGED, 
				BUILD_PINNED, BUILD_UNPINNED };
	}
}
