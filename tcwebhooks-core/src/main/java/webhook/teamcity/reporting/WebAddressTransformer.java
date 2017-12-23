package webhook.teamcity.reporting;

import java.net.URL;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.Data;

public interface WebAddressTransformer {
	
	/**
	 * Extracts the hostname from a URL, generalises it (eg, convert web.api.blah.google.com => google.com)
	 * and returns the generalised version as a string;
	 * Special cases like hostnames without a domain, ipv4 addresses, ipv6Addresses 
	 * @param uri
	 * @return {@link GeneralisedWebAddress}
	 */
	public GeneralisedWebAddress getGeneralisedHostName(URL uri);
	
	@Data
	public static class GeneralisedWebAddress {
		private GeneralisedWebAddressType addressType;
		private String hashedGeneralisedAddress;
		private String generalisedAddress;
		
		public static GeneralisedWebAddress build(String generalisedAddress, GeneralisedWebAddressType type) {
			GeneralisedWebAddress generalisedWebAddress = new GeneralisedWebAddress();
			generalisedWebAddress.setGeneralisedAddress(generalisedAddress);
			generalisedWebAddress.setAddressType(type);
			generalisedWebAddress.setHashedGeneralisedAddress(DigestUtils.sha256Hex(generalisedAddress));
			return generalisedWebAddress;
		}
	}
	
	public enum GeneralisedWebAddressType {
		IPV4_ADDRESS, // Just the first 3 dotted quad and an x, eg 192.168.1.x
		IPV6_ADDRESS, // TODO: Need to only return enough here that it is not identifiable
		HOST_ADDRESS, // For things like "localhost" or "myserver"
		DOMAIN_NAME;  // Domain name, like google.com, slack.com, etc.
	}

}
