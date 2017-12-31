package webhook.teamcity.reporting;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.Data;

@Data
public class GeneralisedWebAddress {
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