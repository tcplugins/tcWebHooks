package webhook.teamcity.server.rest.model.mainconfig;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

/* Use the XmlAttributes on the fields rather than the getters
 * and setters provided by Lombok */
@XmlAccessorType(XmlAccessType.FIELD) 

@Data  // Let Lombok generate the getters and setters.

@XmlRootElement(name = "proxy")
public class Proxy {
	
	@XmlAttribute
	private String host;
	
	@XmlAttribute
	private Integer port;

	@XmlElement(name="noproxy")
	private List<NoProxy> noproxies = new ArrayList<NoProxy>();

	public void addNoProxy(NoProxy noproxy){
		noproxies.add(noproxy);
	}
}
