package org.observertc.webrtc.reporter;

import java.util.function.Consumer;

public interface ConfigProfileProcessor extends Consumer<ConfigProfile> {

	@Override
	default void accept(ConfigProfile config) {
		switch (config) {
			case BIGQUERY:
				this.actionOnBigQuery();
				return;
			default:
				return;
		}
	}

	void actionOnBigQuery();
}
