package webhook.teamcity;

public final class BuildState {
    public static final Integer BUILD_STARTED  			= Integer.parseInt("0000000001",2);
    public static final Integer BUILD_FINISHED 			= Integer.parseInt("0000000010",2);
    public static final Integer BUILD_CHANGED_STATUS 	= Integer.parseInt("0000000100",2);
    public static final Integer BEFORE_BUILD_FINISHED 	= Integer.parseInt("0000001000",2);
    public static final Integer RESPONSIBILITY_CHANGED 	= Integer.parseInt("0000010000",2);
    public static final Integer BUILD_INTERRUPTED 		= Integer.parseInt("0000100000",2);
    public static final Integer BUILD_SUCCESSFUL		= Integer.parseInt("0001000010",2);
    public static final Integer BUILD_FAILED		 	= Integer.parseInt("0010000010",2);
    
    public static final Integer BUILD_FIXED  			= Integer.parseInt("0100000010",2);
    public static final Integer BUILD_BROKEN 			= Integer.parseInt("1000000010",2);
    
    public static final Integer ALL_ENABLED				= Integer.parseInt("0011111011",2);
    
    /**
     * Takes the currentBuildState, for which the WebHook is being triggered
     * and compares it against the build states for which this WebHook is configured
     * to notify.
     * 
     * @param currentBuildState
     * @param buildStatesToNotify
     * @return Whether or not the webhook should trigger for the current build state.
     */
    public static boolean enabled(Integer currentBuildState, Integer buildStatesToNotify) {
		int enabled = (currentBuildState & buildStatesToNotify);  
		return (enabled > 0);
	}
    
    /**
     * Takes the currentBuildState, for which the WebHook is being triggered
     * and compares it against the build states for which this WebHook is configured
     * to notify.
     * 
     * @param currentBuildState
     * @param buildStatesToNotify
     * @return Whether or not the webhook should trigger for the current build state.
     */
    public static boolean exactly(Integer currentBuildState, Integer buildStatesToNotify) {
    	int enabled = (currentBuildState & buildStatesToNotify);  
    	return (enabled == currentBuildState);
    }

    /**
     * Convert build state Integer into short string 
     * 
     * @param  Build state as an Integer constant.
     * @return A string representing the shortname of the state. Is used in messages.
     */
	public static String getShortName(Integer stateInt) {
		
		if (stateInt.equals(BUILD_STARTED)) 		{	return "buildStarted"; }
		if (stateInt.equals(BUILD_FINISHED))		{	return "buildFinished"; }
		if (stateInt.equals(BUILD_CHANGED_STATUS)) 	{ 	return "statusChanged"; }
		if (stateInt.equals(BEFORE_BUILD_FINISHED)) {	return "beforeBuildFinish"; }
		if (stateInt.equals(RESPONSIBILITY_CHANGED)){ 	return "responsibilityChanged"; }
		if (stateInt.equals(BUILD_INTERRUPTED))		{ 	return "buildInterrupted"; }
		if (stateInt.equals(BUILD_SUCCESSFUL))		{ 	return "buildSuccessful"; }
		if (stateInt.equals(BUILD_FAILED))			{ 	return "buildFailed"; }
		if (stateInt.equals(BUILD_FIXED)) 			{	return "buildFixed"; }
		if (stateInt.equals(BUILD_BROKEN))			{	return "buildBroken"; }
		return null;
	}
	
	/**
	 * Convert build state Integer into descriptive string 
	 * 
	 * @param  Build state as an Integer constant.
	 * @return A string that fits into the sentence "The build has...<state>"
	 */
	public static String getDescriptionSuffix(Integer stateInt) {

		if (stateInt.equals(BUILD_STARTED)) 		{	 return "started"; }
		if (stateInt.equals(BUILD_FINISHED)) 		{	 return "finished"; }
		if (stateInt.equals(BUILD_CHANGED_STATUS)) 	{	 return "changed status"; }
		if (stateInt.equals(BEFORE_BUILD_FINISHED)) {	 return "nearly finished"; }
		if (stateInt.equals(RESPONSIBILITY_CHANGED)){	 return "changed responsibility"; }
		if (stateInt.equals(BUILD_INTERRUPTED)) 	{	 return "been interrupted"; }
		if (stateInt.equals(BUILD_SUCCESSFUL))		{ 	 return "completed successfully"; }
		if (stateInt.equals(BUILD_FAILED))			{ 	 return "failed"; }
		if (stateInt.equals(BUILD_FIXED)) 			{	 return "been fixed"; }
		if (stateInt.equals(BUILD_BROKEN)) 			{	 return "broken"; }
		return null;
	}
    
}
