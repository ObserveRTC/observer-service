package org.observertc.webrtc.observer.evaluators.ipflags;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @source: http://www.java2s.com/Code/Java/Network-Protocol/DetermineifthegivenstringisavalidIPv4orIPv6address.htm
 */
public class IPAddressConverter implements Function<String, Optional<InetAddress>> {

	private static final Logger logger = LoggerFactory.getLogger(IPAddressConverter.class);

	private static Pattern VALID_IPV4_PATTERN = null;
	private static Pattern VALID_IPV6_PATTERN = null;
	private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

	static {
		try {
			VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
			VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException e) {
			//logger.severe("Unable to compile pattern", e);
		}
	}

	/**
	 * Determine if the given string is a valid IPv4 or IPv6 address.  This method
	 * uses pattern matching to see if the given string could be a valid IP address.
	 *
	 * @param ipAddress A string that is to be examined to verify whether or not
	 *                  it could be a valid IP address.
	 * @return <code>true</code> if the string is a value that is a valid IP address,
	 * <code>false</code> otherwise.
	 */
	public static boolean isIpAddress(String ipAddress) {

		Matcher m1 = IPAddressConverter.VALID_IPV4_PATTERN.matcher(ipAddress);
		if (m1.matches()) {
			return true;
		}
		Matcher m2 = IPAddressConverter.VALID_IPV6_PATTERN.matcher(ipAddress);
		return m2.matches();
	}


	@Override
	public Optional<InetAddress> apply(String host) {
		if (host == null) {
			return Optional.empty();
		}
		if (!isIpAddress(host)) {
			return Optional.empty();
		}
		try {
			return Optional.of(InetAddress.getByName(host));
		} catch (UnknownHostException e) {
			logger.error("Error occured by converting IP address", e);
			return Optional.empty();
		}
	}
}