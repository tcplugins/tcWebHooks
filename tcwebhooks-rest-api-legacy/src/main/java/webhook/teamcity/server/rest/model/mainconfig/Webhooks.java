package webhook.teamcity.server.rest.model.mainconfig;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

/*
 * <server>
  <webhooks>
    <proxy host="myproxy.mycompany.com" port="8080">
      <noproxy url=".mycompany.com" />
      <noproxy url="192.168.0." />
    </proxy>
    <info url="http://intranet.mycompany.com/docs/UsingWebHooks" text="Using WebHooks in myCompany Inc." />
  </webhooks>
</server>
 */

/* Use the XmlAttributes on the fields rather than the getters
 * and setters provided by Lombok */
@XmlAccessorType(XmlAccessType.FIELD) 

@Data  // Let Lombok generate the getters and setters.

@XmlRootElement(name = "webhooks")
public class Webhooks {
	
	public Webhooks() {
		// TODO Auto-generated constructor stub
	}
	
	public Webhooks(UriInfo uriInfo) {
		// TODO Auto-generated constructor stub
	}
	private Proxy proxy;
	private Information info;
	private String href;
}


