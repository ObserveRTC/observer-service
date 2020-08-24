package org.observertc.webrtc.observer.evaluators.ipflags;

import io.micronaut.context.annotation.Prototype;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.observertc.webrtc.observer.EvaluatorsConfig;

@Prototype
public class IPFlags implements Function<InetAddress, Optional<String>> {

	private List<IPFlag> ipFlags = new LinkedList<>();

	public IPFlags(EvaluatorsConfig.SampleTransformerConfig config) {
		if (config.ipFlags != null && 0 < config.ipFlags.size()) {
			for (EvaluatorsConfig.SampleTransformerConfig.IPFlagConfig ipFlagConfig : config.ipFlags) {
				IPFlag ipFlag = new IPFlag(ipFlagConfig);
				this.ipFlags.add(ipFlag);
			}
		}
	}


	@Override
	public Optional<String> apply(InetAddress inetAddress) {
		for (IPFlag ipFlag : this.ipFlags) {
			if (ipFlag.test(inetAddress)) {
				return Optional.of(ipFlag.getName());
			}
		}
		return Optional.empty();
	}
}
