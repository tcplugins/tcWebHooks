package webhook.teamcity.payload;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import webhook.teamcity.payload.template.WebHookTemplateFromXml;
import webhook.teamcity.settings.WebHookConfigChangeHandler;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ServerPaths;

public class WebHookTemplateFileChangeHandler implements ChangeListener, WebHookConfigChangeHandler {

	final WebHookTemplateManager webHookTemplateManager;
	final WebHookPayloadManager webHookPayloadManager;
	File configFile;
	FileWatcher fw;
	final ServerPaths serverPaths;
	
	public WebHookTemplateFileChangeHandler(
			ServerPaths serverPaths, 
			WebHookTemplateManager webHookTemplateManager, WebHookPayloadManager webHookPayloadManager) {
		this.webHookTemplateManager = webHookTemplateManager;
		this.webHookPayloadManager = webHookPayloadManager;
		this.serverPaths = serverPaths;
		Loggers.SERVER.info("WebHookTemplateFileChangeHandler :: Starting");
	}
	
	public void register(){
		Loggers.SERVER.info("WebHookTemplateFileChangeHandler :: Registering");
		this.configFile = new File(this.serverPaths.getConfigDir() + File.separator + "webhook-templates.xml");
		
		this.fw = new FileWatcher(configFile);

		this.changeOccured("Startup");
		
		this.fw.registerListener(this);
		this.fw.start();
		
		Loggers.SERVER.info("WebHookTemplateFileChangeHandler :: Watching for changes to file: " + this.configFile.getPath());
	}

	@Override
	public void changeOccured(String requestor) {
		Loggers.SERVER.info("WebHookTemplateFileChangeHandler :: Handling change to file: " + this.configFile.getPath() + " requested by " + requestor);
		Loggers.SERVER.debug("WebHookTemplateFileChangeHandler :: My instance is: " + this.toString() + " :: WebHookTemplateManager: " + webHookPayloadManager.toString());
		this.handleConfigFileChange();

	}

	@Override
	public void handleConfigFileChange() {
		List<WebHookTemplate> newTemplates = new ArrayList<>();
		try {
			WebHookTemplates templatesList =  WebHookTemplateJaxHelper.read(configFile.getPath());
			for (webhook.teamcity.settings.entity.WebHookTemplate template : templatesList.getWebHookTemplateList()){
				newTemplates.add(WebHookTemplateFromXml.build(template, webHookPayloadManager));
			}
			this.webHookTemplateManager.unregisterAllXmlConfigTemplates();
			for (WebHookTemplate newTemplate : newTemplates){
				this.webHookTemplateManager.registerTemplateFormatFromXmlConfig(newTemplate);
			}
			
		} catch (FileNotFoundException e) {
			Loggers.SERVER.warn("WebHookTemplateFileChangeHandler :: Exception occurred attempting to reload WebHookTemplates. File not found: " + this.configFile.getPath());
			Loggers.SERVER.debug(e);
		} catch (JAXBException e) {
			Loggers.SERVER.warn("WebHookTemplateFileChangeHandler :: Exception occurred attempting to reload WebHookTemplates. Could not parse: " + this.configFile.getPath());
			Loggers.SERVER.debug(e);
		}
		
	}
	
	

}
