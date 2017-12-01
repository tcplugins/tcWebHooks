package webhook.teamcity.settings.entity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WebHookTemplateJaxHelperImpl implements WebHookTemplateJaxHelper {

	@Override
	public WebHookTemplateEntity readTemplate(String configFilePath) throws JAXBException, FileNotFoundException {
		JAXBContext context = JAXBContext.newInstance(WebHookTemplates.class);
		Unmarshaller um = context.createUnmarshaller();
		File file = new File(configFilePath);
		if (!file.isFile()) {
			throw new FileNotFoundException("File was not a file");
		}
		return (WebHookTemplateEntity) um.unmarshal(file);
	}

	@Override
	public WebHookTemplateEntity readTemplate(InputStream stream) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(WebHookTemplates.class);
		Unmarshaller um = context.createUnmarshaller();
		return (WebHookTemplateEntity) um.unmarshal(stream);
	}

	@Override
	public void writeTemplate(WebHookTemplateEntity template, String configFilePath) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(WebHookTemplateEntity.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(template, new File(configFilePath));
		
	}
	
	@Override
	@Nullable
	public WebHookTemplates readTemplates(@NotNull String configFilePath)
			throws JAXBException, FileNotFoundException {
		JAXBContext context = JAXBContext.newInstance(WebHookTemplates.class);
		Unmarshaller um = context.createUnmarshaller();
		File file = new File(configFilePath);
		if (!file.isFile()) {
			return new WebHookTemplates();
		}
		return (WebHookTemplates) um.unmarshal(file);
	}

	@Override
	@NotNull
	public WebHookTemplates readTemplates(@NotNull InputStream stream)
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(WebHookTemplates.class);
		Unmarshaller um = context.createUnmarshaller();
		return (WebHookTemplates) um.unmarshal(stream);
	}

	@Override
	public void writeTemplates(@NotNull WebHookTemplates templates,
			@NotNull String configFilePath) throws JAXBException {
		
		JAXBContext context = JAXBContext.newInstance(WebHookTemplates.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(templates, new File(configFilePath));
	}

}
