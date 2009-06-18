package webhook.teamcity;

public final class BuildState {
    public static final Integer BUILD_STARTED  			= Integer.parseInt("00000001",2);
    public static final Integer BUILD_FINISHED 			= Integer.parseInt("00000010",2);
    public static final Integer BUILD_CHANGED_STATUS 	= Integer.parseInt("00000100",2);
    public static final Integer BEFORE_BUILD_FINISHED 	= Integer.parseInt("00001000",2);
    public static final Integer RESPONSIBILITY_CHANGED 	= Integer.parseInt("00010000",2);
    public static final Integer BUILD_INTERRUPTED 		= Integer.parseInt("00100000",2);
    
    public static final Integer ALL_ENABLED				= Integer.parseInt("11111111",2);
    
	public static boolean enabled(Integer eventListBitMask, Integer bitMask) {
		 
		return ((eventListBitMask & bitMask) > 0);
	}
    

}
