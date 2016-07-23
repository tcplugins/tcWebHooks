package webhook.teamcity.server.rest.model.template;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;



@XmlRootElement(name = "format")
@XmlType(name = "format", propOrder = {"name", "enabled"})
public class Format {

	public Format() {
	}
	
	@XmlAttribute
	public String name;
	

	@XmlAttribute
	public Boolean enabled;
	
	
}
