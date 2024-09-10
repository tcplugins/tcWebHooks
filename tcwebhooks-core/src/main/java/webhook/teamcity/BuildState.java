package webhook.teamcity;

import static webhook.teamcity.BuildStateEnum.BUILD_BROKEN;
import static webhook.teamcity.BuildStateEnum.BUILD_FAILED;
import static webhook.teamcity.BuildStateEnum.BUILD_FINISHED;
import static webhook.teamcity.BuildStateEnum.BUILD_FIXED;
import static webhook.teamcity.BuildStateEnum.BUILD_SUCCESSFUL;
import static webhook.teamcity.BuildStateEnum.REPORT_STATISTICS;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BuildState {

	Map<BuildStateEnum, BuildStateInterface> states = new EnumMap<>(BuildStateEnum.class);
	
	public BuildState() {
		states.clear();
		
		states.put(BuildStateEnum.BUILD_ADDED_TO_QUEUE, 	new SimpleBuildState(BuildStateEnum.BUILD_ADDED_TO_QUEUE, 		false));
		states.put(BuildStateEnum.BUILD_REMOVED_FROM_QUEUE, new SimpleBuildState(BuildStateEnum.BUILD_REMOVED_FROM_QUEUE,	false));
		states.put(BuildStateEnum.BUILD_STARTED, 			new SimpleBuildState(BuildStateEnum.BUILD_STARTED, 				false));
		states.put(BuildStateEnum.CHANGES_LOADED, 			new SimpleBuildState(BuildStateEnum.CHANGES_LOADED, 			false));
		states.put(BuildStateEnum.BEFORE_BUILD_FINISHED, 	new SimpleBuildState(BuildStateEnum.BEFORE_BUILD_FINISHED, 		false)); 
		states.put(BuildStateEnum.RESPONSIBILITY_CHANGED, 	new SimpleBuildState(BuildStateEnum.RESPONSIBILITY_CHANGED,		false));
		states.put(BuildStateEnum.BUILD_INTERRUPTED, 		new SimpleBuildState(BuildStateEnum.BUILD_INTERRUPTED, 			false));
		states.put(BuildStateEnum.BUILD_SUCCESSFUL, 		new SimpleBuildState(BuildStateEnum.BUILD_SUCCESSFUL, 			false));
		states.put(BuildStateEnum.BUILD_FAILED, 			new SimpleBuildState(BuildStateEnum.BUILD_FAILED, 				false));

		states.put(BuildStateEnum.BUILD_BROKEN, 			new SimpleBuildState(BuildStateEnum.BUILD_BROKEN, 				false));
		states.put(BuildStateEnum.BUILD_FIXED, 				new SimpleBuildState(BuildStateEnum.BUILD_FIXED, 				false));
		
		//states.put(BuildStateEnum.BUILD_FINISHED, 			new SimpleBuildState(BuildStateEnum.BUILD_FINISHED, 			false)); 
		states.put(BuildStateEnum.BUILD_PINNED, 			new SimpleBuildState(BuildStateEnum.BUILD_PINNED, 				false)); 
		states.put(BuildStateEnum.BUILD_UNPINNED, 			new SimpleBuildState(BuildStateEnum.BUILD_UNPINNED, 			false)); 
		states.put(BuildStateEnum.TESTS_MUTED, 			new SimpleBuildState(BuildStateEnum.TESTS_MUTED, 			false)); 
		states.put(BuildStateEnum.TESTS_UNMUTED, 			new SimpleBuildState(BuildStateEnum.TESTS_UNMUTED, 			false)); 
		states.put(BuildStateEnum.SERVICE_MESSAGE_RECEIVED, new SimpleBuildState(BuildStateEnum.SERVICE_MESSAGE_RECEIVED, 	false)); 
		states.put(BuildStateEnum.REPORT_STATISTICS, 		new SimpleBuildState(BuildStateEnum.REPORT_STATISTICS, 			false)); 
	}
	
	/**
	 * Get a Set of all the possible buildStates. Regardless of whether they are enabled or not.
	 * @return Set of all possible build states.
	 */
	public Set<BuildStateEnum> getStateSet(){
		return states.keySet();
	}
	
	public Set<BuildStateEnum> getEnabledBuildStates() {
	    return Arrays.stream(BuildStateEnum.values()).filter(this::enabled).collect(Collectors.toSet());
	}
	public Set<BuildStateEnum> getEnabledNotifiableBuildStates() {
	    return Arrays.stream(BuildStateEnum.getNotifyStates()).filter(this::enabled).collect(Collectors.toSet());
	}
	
    /**
     * Takes the currentBuildState, for which the WebHook is being triggered
     * and compares it against the build states for which this WebHook is configured
     * to notify.<br>
     * If webhook is triggering on TeamCity's Finished event, any of the "finished" states are valid.
     * 
     * @param currentBuildState
     * @return Whether or not the webhook should trigger for the current build state.
     */
    public boolean enabled(BuildStateEnum currentBuildState) {
        if (BuildStateEnum.BUILD_FINISHED.equals(currentBuildState)) {
            return isAnyFinishedStateEnabled();
        }
    	return states.containsKey(currentBuildState) && states.get(currentBuildState).isEnabled();
	}

    public boolean isAnyFinishedStateEnabled() {
        // If webhook is triggering on finished event, any of the "finished" states are valid.
        return states.get(BUILD_FAILED).isEnabled() 
                || states.get(BUILD_FIXED).isEnabled() 
                || states.get(BUILD_SUCCESSFUL).isEnabled() 
                || states.get(BUILD_FIXED).isEnabled();
    }
    
    public boolean enabled(BuildStateEnum currentBuildState, boolean success, boolean changed){
    	if (currentBuildState != BuildStateEnum.BUILD_FINISHED){
    		return enabled(currentBuildState);
    	} else {
    		if (enabled(BUILD_SUCCESSFUL) &&  enabled(BUILD_FIXED) && changed && success){
    			return true;
    		}
    		if (enabled(BUILD_SUCCESSFUL) && !enabled(BUILD_FIXED) && success){
    			return true;
    		}
    		if (enabled(BUILD_FAILED) && enabled(BUILD_BROKEN) && changed && !success){
    			return true;
    		}
    		if (enabled(BUILD_FAILED) && !enabled(BUILD_BROKEN) && !success){
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Determine the WebHook build state from a TeamCity build event state. For anything except finished
     * this will simply return the TeamCity state.<br/>
     * However, for finished builds, determine if it was success or failure, and then with builds that changed state, if it was a fix or break.  
     * 
     * @param currentBuildState
     * @param success
     * @param changed
     * @return The WebHook state for use determining template etc.
     */
    public static BuildStateEnum getEffectiveState(BuildStateEnum currentBuildState, boolean success, boolean changed){
    	
    	if (currentBuildState.equals(BUILD_FINISHED)){
    		if (success){
    			if (changed){
    				return BUILD_FIXED;
    			}
    			return BUILD_SUCCESSFUL;
    		} else {
    			if (changed){
    				return BUILD_BROKEN;
    			}
    			return BUILD_FAILED;
    		}
    	}
    	return currentBuildState;
    }
    
    public BuildState setEnabled(BuildStateEnum currentBuildState, boolean enabled){
    	if (enabled)
    		return enable(currentBuildState);
    	else
    		return disable(currentBuildState);
    }
    
    /**
     * Enable all build events for notification
     * Note: BROKEN and FIXED restrict builds, so don't set those.
     */
    public BuildState setAllEnabled(){
    	for (BuildStateEnum state : states.keySet()){
    		switch (state){
    		case BUILD_BROKEN:
    			disable(state);
    			break;
    		case BUILD_FIXED:
    			disable(state);
    			break;
    		case REPORT_STATISTICS:		// This is not available to templates, so it makes no sense to include it in "all"
    			disable(state);
    			break;
    		default:
    			enable(state);
    			break;
    		}
    	}
    	return this;
    }
    
    public BuildState enable(BuildStateEnum currentBuildState){
    	if (currentBuildState != null && !BuildStateEnum.BUILD_FINISHED.equals(currentBuildState)) {
    		states.get(currentBuildState).enable();
    	}
    	return this;
    }

    public BuildState disable(BuildStateEnum currentBuildState){
    	if (currentBuildState != null && !BuildStateEnum.BUILD_FINISHED.equals(currentBuildState)) {
    		states.get(currentBuildState).disable();
    	}
    	return this;
    }

    /**
     * Convert build state Integer into short string 
     * 
     * @param  Build state as an enum.
     * @return A string representing the shortname of the state. Is used in messages.
     */
	public String getShortName(BuildStateEnum state) {
			return state.getShortName();
	}
	
	/**
	 * Convert build state Integer into descriptive string 
	 * 
	 * @param  Build state as an Integer constant.
	 * @return A string that fits into the sentence "The build has...<state>"
	 */
	public String getDescriptionSuffix(BuildStateEnum state) {
		return state.getDescriptionSuffix();
	}

	/**
	 * Determines whether all states are enabled. Eg, for any state whether the webhook is enabled.
	 * This handles cases where BUILD_BROKEN or BUILD_FIXED are enabled, which means that the webhook
	 * might not fire depending on the build status delta.
	 * @return Whether every buildState is effectively enabled.
	 */
	public boolean allEnabled() {
		boolean areAllEnbled = true;
		for (Map.Entry<BuildStateEnum,BuildStateInterface> state : states.entrySet()){
			if (state.getKey().equals(BUILD_BROKEN)){
				if (state.getValue().isEnabled()){
					return false;
				}
				continue;
			}
			if (state.getKey().equals(BUILD_FIXED)){
				if (state.getValue().isEnabled()){
					return false;
				}
				continue;
			}
			if (state.getKey().equals(REPORT_STATISTICS)){
				if (state.getValue().isEnabled()){
					return false;
				}
				continue;
			}
			areAllEnbled = areAllEnbled && state.getValue().isEnabled();
		}
		return areAllEnbled;
	}

	public boolean noneEnabled() {
		int enabled = 0;
		for (Map.Entry<BuildStateEnum,BuildStateInterface> state : states.entrySet()){
			if (state.getKey().equals(BUILD_FINISHED)){
				if (isAnyFinishedStateEnabled()){
					enabled++;
				}
				continue;
			}
			if (state.getValue().isEnabled())  
				enabled++;
		}
		return enabled == 0;
	}
	
}
