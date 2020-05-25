package com.observertc.gatekeeper.dto;

import java.util.UUID;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class DemoEvaluationDTO {
	//	@BsonId
	@BsonProperty("id")
	public UUID uuid;

	public UUID conferenceUUID;
	public Integer median;
}
