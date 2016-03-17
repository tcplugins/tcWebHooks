package netwolfuk.teamcity.plugins.tcwebhooks.template.builder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;

import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItem;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateState;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;

public class TemplateGenerator {
	
	/**
	 * Takes an XML file describing a template and generates a bunch of JSON 
	 * files representing the various templates for various build states.
	 * 
	 *  See BuildFlowDockTemplates() method in the {@link BuildFlowDockTemplateFiles} test
	 *  for an example usage.
	 *  
	 * @param templateName
	 * @param templateFileLocation
	 * @param targetFileLocation
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void generate(String templateName, String templateFileLocation, String targetFileLocation) throws JAXBException, IOException{
		WebHookTemplates templatesList =  WebHookTemplateJaxHelper.read(templateFileLocation);
		
		for (WebHookTemplateEntity template : templatesList.getWebHookTemplateList()){
			if (template.isEnabled() && template.getName().equals(templateName)){
				
				File defaultTemplateFile = new File(targetFileLocation + "/" + templateName + "-default-normal.json");
				File defaultBranchTemplateFile = new File(targetFileLocation + "/" + templateName + "-default-branch.json");
				if (template.getDefaultTemplate() != null){
					FileUtils.writeStringToFile(defaultTemplateFile, template.getDefaultTemplate().trim());
				}
				if (template.getDefaultBranchTemplate() != null){
					FileUtils.writeStringToFile(defaultBranchTemplateFile, template.getDefaultBranchTemplate().trim());
				}
				
				for (WebHookTemplateItem item : template.getTemplates()){
					String templateFileName = buildFileName(item.getStates());
					File templateFile = new File(targetFileLocation + "/" + templateName + "-"+ templateFileName +"-normal.json");
					File branchTemplateFile = new File(targetFileLocation + "/" + templateName + "-"+ templateFileName +"-branch.json");
					
					FileUtils.writeStringToFile(templateFile, item.getTemplateText().trim());
					FileUtils.writeStringToFile(branchTemplateFile, item.getBranchTemplateText().trim());
				}
			}
		}
	}
	
	protected String buildFileName(List<WebHookTemplateState> states) {
		StringBuilder filename = new StringBuilder();
		for (WebHookTemplateState state: states){
			filename.append("-").append(state.getType());
		}
		return filename.substring(1);
	}
}

