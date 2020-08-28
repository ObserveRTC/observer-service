package org.observertc.webrtc.observer.dto;

import io.micronaut.core.annotation.Introspected;
import java.util.UUID;

@Introspected
public class ObserverDTO {
	public UUID uuid;
	public String name;
	public String description;

}
