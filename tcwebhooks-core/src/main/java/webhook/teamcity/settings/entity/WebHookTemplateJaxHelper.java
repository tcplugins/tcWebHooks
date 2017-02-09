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

public class WebHookTemplateJaxHelper {

	private WebHookTemplateJaxHelper(){}

	/**
	 * Read saved configuration from file
	 * 
	 * @return {@link WebHookTemplates} bean
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	@Nullable
	public static WebHookTemplates read(@NotNull String configFilePath)
			throws JAXBException, FileNotFoundException {
		JAXBContext context = JAXBContext.newInstance(WebHookTemplates.class);
		Unmarshaller um = context.createUnmarshaller();
		File file = new File(configFilePath);
		if (!file.isFile()) {
			return new WebHookTemplates();
		}
		return (WebHookTemplates) um.unmarshal(file);
	}

	/**
	 * Read saved configuration from stream
	 * 
	 * @return Suppliers bean
	 * @throws JAXBException
	 */
	@NotNull
	public static WebHookTemplates read(@NotNull InputStream stream)
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(WebHookTemplates.class);
		Unmarshaller um = context.createUnmarshaller();
		return (WebHookTemplates) um.unmarshal(stream);
	}

	/**
	 * Read saved configuration from file and supplement with missing suppliers.
	 * 
	 * @return Suppliers bean
	 */
	public static WebHookTemplates load(@NotNull String serverConfigDir,
			@NotNull Map<String, WebHookTemplateFromXml> templates) {
		// Load previously saved suppliers from config
		WebHookTemplates suppliersBean;
		try {
			suppliersBean = WebHookTemplateJaxHelper.read(serverConfigDir);
		} catch (Exception e) {
			Loggers.SERVER.error(e.getMessage());
			suppliersBean = new WebHookTemplates();
		}

		assert suppliersBean != null;
		List<String> templateNames = new ArrayList<>();
		List<WebHookTemplate> itemsToRemove = new ArrayList<>();

		// Get supplier instance by id. If it's not found - remove it
		for (WebHookTemplate templateBean : suppliersBean.getWebHookTemplateList()) {
			WebHookTemplateFromXml supplier = templates.get(templateBean.getName());
			if (null == supplier) {
				Loggers.SERVER.error(String.format(
						"Failed to get template by %s name!",
						templateBean.getName()));
				itemsToRemove.add(templateBean);
				continue;
			}
			templateNames.add(templateBean.getName());
		}
		suppliersBean.getWebHookTemplateList().removeAll(itemsToRemove);

		// iterate over templates loaded from beans and check, that template was
		// saved in config
		for (String id : templates.keySet()) {
			if (!templateNames.contains(id)) {
				suppliersBean.addWebHookTemplate(new WebHookTemplate(id, true));
			}
		}

		return suppliersBean;
	}

	/**
	 * Write suppliers bean to configuration file
	 * 
	 * @throws JAXBException
	 */
	public static void write(@NotNull WebHookTemplates templates,
			@NotNull String configFilePath) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(templates.getClass());
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(templates, new File(configFilePath));
	}
}
