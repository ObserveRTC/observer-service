package org.observertc.webrtc.observer.evaluators.ipflags;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IPFlag implements Predicate<InetAddress> {

	private static final Logger logger = LoggerFactory.getLogger(IPFlag.class);

	private final String name;
	private final List<IPEntry> entries;

	public IPFlag(EvaluatorsConfig.SampleTransformerConfig.IPFlagConfig config) {
		this.name = config.name;
		this.entries = new LinkedList<>();
		if (config.networks != null && 0 < config.networks.size()) {
			for (String network : config.networks) {
				Optional<IPEntry> ipEntryHolder = this.parse(network);
				if (!ipEntryHolder.isPresent()) {
					continue;
				}
				this.entries.add(ipEntryHolder.get());
			}
		}
	}

	public String getName() {
		return this.name;
	}

	@Override
	public boolean test(InetAddress inetAddress) {
		byte[] ipAddress = inetAddress.getAddress();
		for (IPEntry ipEntry : this.entries) {
			if (ipEntry.address.length != ipAddress.length) {
				continue;
			}
			int passedBytes = 0;
			for (; passedBytes < ipAddress.length; ++passedBytes) {
				byte maskedIP = ipAddress[passedBytes];
				maskedIP &= ipEntry.netMask[passedBytes];
				if (maskedIP != ipEntry.address[passedBytes]) {
					break;
				}
			}
			if (passedBytes == ipAddress.length) {
				return true;
			}
		}
		return false;
	}

	private Optional<IPEntry> parse(String network) {
		IPEntry result = new IPEntry();
		IPAddressConverter ipAddressConverter = new IPAddressConverter();
		String[] parts = network.split("/", 2);
		if (parts.length != 2) {
			logger.error("Unable to parse network string: {}. The number of parts (part1/part2) is not 2", network);
			return Optional.empty();
		}
		Optional<InetAddress> addressHolder = ipAddressConverter.apply(parts[0]);
		if (!addressHolder.isPresent()) {
			logger.error("Unable to parse the address of the network for {}", parts[0]);
			return Optional.empty();
		}
		Optional<InetAddress> netMaskHolder = ipAddressConverter.apply(parts[1]);
		if (!netMaskHolder.isPresent()) {
			logger.error("Unable to parse the address of the network for {}", parts[1]);
			return Optional.empty();
		}
		result.address = addressHolder.get().getAddress();
		result.netMask = netMaskHolder.get().getAddress();
		return Optional.of(result);
	}

	class IPEntry {
		byte[] address;
		byte[] netMask;
	}
}
