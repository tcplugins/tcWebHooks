package webhook.teamcity.history;

import lombok.Data;

@Data
public class GeneralisedWebAddress {
	private GeneralisedWebAddressType addressType;
	private String generalisedAddress;

	public static GeneralisedWebAddress build(String generalisedAddress, GeneralisedWebAddressType type) {
		GeneralisedWebAddress generalisedWebAddress = new GeneralisedWebAddress();
		generalisedWebAddress.setGeneralisedAddress(generalisedAddress);
		generalisedWebAddress.setAddressType(type);
		return generalisedWebAddress;
	}
}