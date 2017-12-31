package webhook.teamcity.reporting;

public enum GeneralisedWebAddressType {
	IPV4_ADDRESS, // Just the first 3 dotted quad and an x, eg 192.168.1.x
	IPV6_ADDRESS, // TODO: Need to only return enough here that it is not identifiable
	HOST_ADDRESS, // For things like "localhost" or "myserver"
	DOMAIN_NAME;  // Domain name, like google.com, slack.com, etc.
}