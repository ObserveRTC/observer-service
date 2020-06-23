package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.builders.AbstractBuilder;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class StatsPayloadProcessorConfigProvider extends AbstractBuilder {

	public static class Config {
		public Map<String, Object> participants;
	}
}
