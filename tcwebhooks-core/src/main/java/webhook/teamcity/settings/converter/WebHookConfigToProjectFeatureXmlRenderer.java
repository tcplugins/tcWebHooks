package webhook.teamcity.settings.converter;

import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import lombok.AllArgsConstructor;
import webhook.teamcity.TeamCityCoreFacade;
import webhook.teamcity.settings.ProjectFeatureToWebHookConfigConverter;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.entity.ProjectFeatureEntity;
import webhook.teamcity.settings.entity.ProjectFeatureEntity.ProjectFeatureExtension;

@AllArgsConstructor
public class WebHookConfigToProjectFeatureXmlRenderer {
    
    private ProjectFeatureToWebHookConfigConverter myProjectFeatureToWebHookConfigConverter;
    private TeamCityCoreFacade myTeamCityCoreFacade;

    public String renderAsXml(List<WebHookConfig> webHookConfigs, SProject sProject) throws JAXBException {
        AtomicInteger nextIdValue = new AtomicInteger(myTeamCityCoreFacade.getMaxDescripterId(sProject));
        JAXBContext context = JAXBContext.newInstance(ProjectFeatureEntity.class);
        Marshaller m = context.createMarshaller();
        StringWriter sw = new StringWriter();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        
        webHookConfigs.stream().forEach(w ->  {
            if (w.getFeatureDescriptorId() == null) {
                w.setFeatureDescriptorId("PROJECT_EXT_" + nextIdValue.addAndGet(1));
            }
        });
        
        List<SProjectFeatureDescriptor> webhooksAsProjectFeatures = webHookConfigs
                .stream()
                .map(whc -> myProjectFeatureToWebHookConfigConverter.convert(whc))
                .collect(Collectors.toList());
        
        ProjectFeatureEntity e = new ProjectFeatureEntity();
        e.setProjectExtensions(webhooksAsProjectFeatures.stream().map(
            f -> ProjectFeatureExtension.init(f.getId(), f.getType(), f.getParameters())
            ).collect(Collectors.toList()));
        
        m.marshal(e, sw);
        return sw.toString();
    }

}
