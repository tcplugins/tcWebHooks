package webhook.teamcity.server.rest.model.mainconfig;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import jetbrains.buildServer.server.rest.util.BeanContext;
import lombok.Data;

/*
 * <info url="http://intranet.mycompany.com/docs/UsingWebHooks" text="Using WebHooks in myCompany Inc." />
 */


/* Use the XmlAttributes on the fields rather than the getters
 * and setters provided by Lombok */
@XmlAccessorType(XmlAccessType.FIELD) 

@Data  // Let Lombok generate the getters and setters.
@XmlRootElement(name = "info")
public class Information {
	public Information(UriInfo uriInfo) {
		// TODO Auto-generated constructor stub
	}

	public Information() {
		// TODO Auto-generated constructor stub
	}

	@XmlAttribute 
	String url;
	
	@XmlAttribute
	String text;
	
	@XmlAttribute(name="further-reading")
	Boolean furtherReading;
	
}
