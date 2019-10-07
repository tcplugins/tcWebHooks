package webhook.teamcity.payload;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ServerPaths;
import webhook.teamcity.DeferrableService;
import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.settings.WebHookConfigChangeHandler;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;

public class WebHookTemplateFileChangeHandler implements ChangeListener, WebHookConfigChangeHandler, DeferrableService {

	final WebHookTemplateManager webHookTemplateManager;
	final WebHookPayloadManager webHookPayloadManager;
	final WebHookTemplateJaxHelper webHookTemplateJaxHelper;
	final DeferrableServiceManager deferrableServiceManager;
	File configFile;
	FileWatcher fw;
	final ServerPaths serverPaths;
	
	public WebHookTemplateFileChangeHandler(
			ServerPaths serverPaths, 
			WebHookTemplateManager webHookTemplateManager, WebHookPayloadManager webHookPayloadManager, WebHookTemplateJaxHelper webHookTemplateJaxHelper, DeferrableServiceManager deferrableServiceManager) {
		this.webHookTemplateManager = webHookTemplateManager;
		this.webHookPayloadManager = webHookPayloadManager;
		this.webHookTemplateJaxHelper = webHookTemplateJaxHelper;
		this.serverPaths = serverPaths;
		this.deferrableServiceManager = deferrableServiceManager;
		Loggers.SERVER.debug("WebHookTemplateFileChangeHandler :: Starting");
	}
	
	public void requestDeferredRegistration() {
		Loggers.SERVER.info("WebHookTemplateFileChangeHandler :: Registering as a deferrable service");
		deferrableServiceManager.registerService(this);
	}
	
	public void register(){
		Loggers.SERVER.debug("WebHookTemplateFileChangeHandler :: Registering");
		this.configFile = new File(this.serverPaths.getConfigDir() + File.separator + "webhook-templates.xml");
		
		this.fw = new FileWatcher(configFile);
		this.webHookTemplateManager.setConfigFilePath(this.configFile.getAbsolutePath());

		this.changeOccured("Startup");
		
		this.fw.registerListener(this);
		this.fw.start();
		
		Loggers.SERVER.info("WebHookTemplateFileChangeHandler :: Watching for changes to file: " + this.configFile.getPath());
	}

	@Override
	public void changeOccured(String requestor) {
		Loggers.SERVER.info("WebHookTemplateFileChangeHandler :: Handling change to file: " + this.configFile.getPath() + " requested by " + requestor);
		Loggers.SERVER.debug("WebHookTemplateFileChangeHandler :: My instance is: " + this.toString() + " :: WebHookTemplateManager: " + webHookTemplateManager.toString());
		this.handleConfigFileChange();

	}

	@Override
	public void handleConfigFileChange() {
		try {
			WebHookTemplates templatesList =  webHookTemplateJaxHelper.readTemplates(configFile.getPath());
			this.webHookTemplateManager.registerAllXmlTemplates(templatesList);
		} catch (FileNotFoundException e) {
			Loggers.SERVER.warn("WebHookTemplateFileChangeHandler :: Exception occurred attempting to reload WebHookTemplates. File not found: " + this.configFile.getPath());
			Loggers.SERVER.debug(e);
		} catch (JAXBException e) {
			Loggers.SERVER.warn("WebHookTemplateFileChangeHandler :: Exception occurred attempting to reload WebHookTemplates. Could not parse: " + this.configFile.getPath());
			Loggers.SERVER.debug(e);
		}
		
	}
	
	

}
