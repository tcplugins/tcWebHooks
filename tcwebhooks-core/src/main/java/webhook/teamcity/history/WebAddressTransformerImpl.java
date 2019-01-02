package webhook.teamcity.history;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

import org.jetbrains.annotations.Nullable;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

public class WebAddressTransformerImpl implements WebAddressTransformer {

	@Override
	public GeneralisedWebAddress getGeneralisedHostName(URL url) {
		String host = url.getHost();
		InetAddress ip = extractInetAddress(host);
		if (ip != null) {
			if (ip instanceof Inet4Address ) {
				return GeneralisedWebAddress.build(host.replaceFirst("\\d+$", ""), GeneralisedWebAddressType.IPV4_ADDRESS);
			} else if (ip instanceof Inet6Address) {
				return GeneralisedWebAddress.build(ip.getHostAddress(), GeneralisedWebAddressType.IPV6_ADDRESS);
			}
		} else if (InternetDomainName.isValid(host)) {
			InternetDomainName domainName = InternetDomainName.from(host);
			if (domainName.isUnderPublicSuffix()) {
				return GeneralisedWebAddress.build(domainName.topPrivateDomain().toString(), GeneralisedWebAddressType.DOMAIN_NAME);
			} else if (domainName.hasParent()) {
				return GeneralisedWebAddress.build(domainName.parent().toString(), GeneralisedWebAddressType.DOMAIN_NAME);
			}
			return GeneralisedWebAddress.build(host, GeneralisedWebAddressType.HOST_ADDRESS);
		}

		return null;
	}

	@Override
	public GeneralisedWebAddress getGeneralisedHostName(String url) {
		try {
			if (url != null && !url.trim().isEmpty()) {
				return getGeneralisedHostName(new URL(url));
			} else {
				return getGeneralisedHostName(new URL("http://undefined"));
			}
		} catch (MalformedURLException e) {
			return GeneralisedWebAddress.build("invalidUrl", GeneralisedWebAddressType.HOST_ADDRESS);
		}
	}
	
	@Nullable
	private InetAddress extractInetAddress(String host) {
		if (InetAddresses.isInetAddress(host)) {
			return InetAddresses.forString(host);
		}
		if (host.startsWith("[") && host.endsWith("]") && InetAddresses.isInetAddress(host.substring(1, host.length()-1))) {
			return InetAddresses.forString(host.substring(1, host.length()-1));
		}
		return null;
	}

}
