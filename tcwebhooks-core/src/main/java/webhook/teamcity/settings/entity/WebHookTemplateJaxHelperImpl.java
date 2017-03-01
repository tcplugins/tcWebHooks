package webhook.teamcity.settings.entity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import webhook.teamcity.Loggers;
import webhook.teamcity.payload.template.WebHookTemplateFromXml;

public class WebHookTemplateJaxHelperImpl implements WebHookTemplateJaxHelper {

	/* (non-Javadoc)
	 * @see webhook.teamcity.settings.entity.WebHookTemplateJaxHelper#read(java.lang.String)
	 */
	@Override
	@Nullable
	public WebHookTemplates read(@NotNull String configFilePath)
			throws JAXBException, FileNotFoundException {
		JAXBContext context = JAXBContext.newInstance(WebHookTemplates.class);
		Unmarshaller um = context.createUnmarshaller();
		File file = new File(configFilePath);
		if (!file.isFile()) {
			return new WebHookTemplates();
		}
		return (WebHookTemplates) um.unmarshal(file);
	}

	/* (non-Javadoc)
	 * @see webhook.teamcity.settings.entity.WebHookTemplateJaxHelper#read(java.io.InputStream)
	 */
	@Override
	@NotNull
	public WebHookTemplates read(@NotNull InputStream stream)
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(WebHookTemplates.class);
		Unmarshaller um = context.createUnmarshaller();
		return (WebHookTemplates) um.unmarshal(stream);
	}

	/* (non-Javadoc)
	 * @see webhook.teamcity.settings.entity.WebHookTemplateJaxHelper#write(webhook.teamcity.settings.entity.WebHookTemplates, java.lang.String)
	 */
	@Override
	public void write(@NotNull WebHookTemplates templates,
			@NotNull String configFilePath) throws JAXBException {
		
		JAXBContext context = JAXBContext.newInstance(WebHookTemplates.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(templates, new File(configFilePath));
	}
}
