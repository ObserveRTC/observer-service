package org.observertc.webrtc.observer.service.processors;

import org.observertc.webrtc.observer.service.builders.AbstractBuilder;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class StatsPayloadProcessorConfigProvider extends AbstractBuilder {

	public static class Config {
		public Map<String, Object> participants;
	}
}
