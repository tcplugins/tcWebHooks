package webhook.teamcity.payload.template;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;

public abstract class AbstractFileSetBasedWebHookTemplate extends AbstractWebHookTemplate {
	
	Map<BuildStateEnum,WebHookTemplateContent> templateContent = new HashMap<>();
	Map<BuildStateEnum,WebHookTemplateContent> branchTemplateContent = new HashMap<>();

	public abstract String getLoggingName();
	public abstract String getTemplateFilesLocation();
	
	public abstract Map<BuildStateEnum, String> getNormalTemplateMap();
	public abstract Map<BuildStateEnum, String> getBranchTemplateMap();

	public AbstractFileSetBasedWebHookTemplate(WebHookTemplateManager manager) {
		setTemplateManager(manager);
	}
	
	@Override
	public void register() {
		templateContent.clear();
		branchTemplateContent.clear();
		loadTemplatesFromFileSet();
		if (!templateContent.isEmpty() && !branchTemplateContent.isEmpty()){
			super.register(this);
		} else {
			if (templateContent.isEmpty()){
				Loggers.SERVER.error(getLoggingName() + " :: Failed to register template " + getTemplateShortName() + ". No regular template configurations were found.");
			}
			if (branchTemplateContent.isEmpty()){
				Loggers.SERVER.error(getLoggingName() + " :: Failed to register template " + getTemplateShortName() + ". No branch template configurations were found.");
			}
		}
	}

	private URL findPropertiesFileUrlInVariousClassloaders(String propertiesFile) {
		final ClassLoader[] classLoaders = {AbstractFileSetBasedWebHookTemplate.class.getClassLoader(), ClassLoader.getSystemClassLoader()}; 
		URL url = null;
		for (ClassLoader cl : classLoaders){
			if (cl != null){
				url = cl.getResource(propertiesFile);
		        if (url != null){
		        	break;
		        }
			}
		}
		return url;
	}
	
	private static final int BUFFER_SIZE = 4 * 1024;

	public static String inputStreamToString(InputStream inputStream, String charsetName)
	        throws IOException {
	    StringBuilder builder = new StringBuilder();
	    InputStreamReader reader = new InputStreamReader(inputStream, charsetName);
	    char[] buffer = new char[BUFFER_SIZE];
	    int length;
	    while ((length = reader.read(buffer)) != -1) {
	        builder.append(buffer, 0, length);
	    }
	    return builder.toString();
	}
	
	public String readFully(InputStream inputStream, String encoding)
	        throws IOException {
	    return new String(readFully(inputStream), encoding);
	}    

	private byte[] readFully(InputStream inputStream)
	        throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024];
	    int length = 0;
	    while ((length = inputStream.read(buffer)) != -1) {
	        baos.write(buffer, 0, length);
	    }
	    return baos.toByteArray();
	}

    private String readFileContents(String templateFile){
    	URL url = findPropertiesFileUrlInVariousClassloaders(templateFile);
    	if (url != null) {
    		InputStream in = null;
	        try {
	        	in = url.openStream();
	        	return inputStreamToString(in, "UTF-8");
	        } catch (IOException e) {
	        	Loggers.SERVER.error(getLoggingName() + " :: An Error occurred trying to load the template file: " + templateFile + ".");
	        	Loggers.SERVER.debug(e);
	        	
	        } finally {
	           try {
	        	   if (in != null){
	        		   in.close();
	        	   }
				} catch (IOException e) {
					Loggers.SERVER.error(e);
				}
	        }
	    } else {
	    	Loggers.SERVER.error(getLoggingName() + " :: An Error occurred trying to load the template file: " + templateFile + ". The file was not found in the classpath.");
	    }
    	return null;
    }

	/**
	 * Load the template from a file, rather than doing silly string escaping in java.
	 */
	private void loadTemplatesFromFileSet() {
		for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
			if (getNormalTemplateMap().containsKey(state)){
				String templateContents = readFileContents(getTemplateFilesLocation() + getNormalTemplateMap().get(state));
				if (templateContents != null) {
	    			templateContent.put(state, WebHookTemplateContent.create(
							state.getShortName(), 
							templateContents,
							true,
							this.getPreferredDateTimeFormat()));
					Loggers.SERVER.info(getLoggingName() + " :: Found and loaded normal template for: " + state.getShortName());
					Loggers.SERVER.debug(getLoggingName() + " :: Template content is: " + templateContents);

				}
			}
	    } 
		for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
			if (getBranchTemplateMap().containsKey(state)){
				String templateContents = readFileContents(getTemplateFilesLocation() + getBranchTemplateMap().get(state));
				if (templateContents != null) {
					branchTemplateContent.put(state, WebHookTemplateContent.create(
							state.getShortName(), 
							templateContents,
							true,
							this.getPreferredDateTimeFormat()));
					Loggers.SERVER.info(getLoggingName() + " :: Found and loaded branch template for: " + state.getShortName());
					Loggers.SERVER.debug(getLoggingName() + " :: Template content is: " + templateContents);
					
				}
			}
		} 
	}

	@Override
	public WebHookTemplateContent getTemplateForState(BuildStateEnum buildState) {
		if (templateContent.containsKey(buildState)){
			return (templateContent.get(buildState)).copy(); 
		}
		return null;
	}

	@Override
	public WebHookTemplateContent getBranchTemplateForState(BuildStateEnum buildState) {
		if (branchTemplateContent.containsKey(buildState)){
			return (branchTemplateContent.get(buildState)).copy(); 
		}
		return null;
	}

	@Override
	public Set<BuildStateEnum> getSupportedBuildStates() {
		return templateContent.keySet();
	}

	@Override
	public Set<BuildStateEnum> getSupportedBranchBuildStates() {
		return branchTemplateContent.keySet();
	}
	
}
