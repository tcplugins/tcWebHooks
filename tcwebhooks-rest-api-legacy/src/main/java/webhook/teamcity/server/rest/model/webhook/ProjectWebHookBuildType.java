package webhook.teamcity.server.rest.model.webhook;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

@XmlRootElement(name="buildType")
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@Getter @Setter
public class ProjectWebHookBuildType {

    @XmlAttribute
    private Boolean allEnabled;

    @XmlAttribute
    private Boolean subProjectsEnabled;

    @XmlElement(name = "id") @XmlElementWrapper(name = "enabledBuildTypes")
    private Collection<String> enabledBuildTypes;
}
