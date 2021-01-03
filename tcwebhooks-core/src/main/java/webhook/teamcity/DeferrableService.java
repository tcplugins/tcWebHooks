package webhook.teamcity;

public interface DeferrableService {
	
	/**
	 * Register with the deferred startup service. 
	 * Later it will invoke the deferredStart method.
	 */
	public void requestDeferredRegistration();
	
	/**
	 * Run any initialisation code after TeamCity has started.
	 */
	public void register();
	
	/**
	 * Run any shutdown code when the plug-in is unloaded or TeamCity is shutting down.
	 */
	public void unregister();

}
