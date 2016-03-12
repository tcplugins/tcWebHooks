package webhook.teamcity.server.rest.model.mainconfig;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

/* Use the XmlAttributes on the fields rather than the getters
 * and setters provided by Lombok */
@XmlAccessorType(XmlAccessType.FIELD) 

@Data  // Let Lombok generate the getters and setters.

@XmlType (name="noproxy")
public class NoProxy {
	@XmlAttribute
	String url;

	/**
	 * No Arg constructor for JAXB.
	 */
	public NoProxy () {
	}
	
	public NoProxy(String noProxyUrl) {
		this.url = noProxyUrl;
	}

}
