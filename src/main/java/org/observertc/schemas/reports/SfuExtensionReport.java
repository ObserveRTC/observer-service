package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for Extended provided arbitrary data.
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class SfuExtensionReport {
	public static final String VERSION="2.0.0-beta.54";
	public static Builder newBuilder() {
		return new Builder();
	}
	/**
	* The service id the report belongs to
	*/
	@JsonProperty("serviceId")
	public String serviceId;
	/**
	* The media unit id the report belongs to
	*/
	@JsonProperty("mediaUnitId")
	public String mediaUnitId;
	/**
	* The marker the originated sample is reported with
	*/
	@JsonProperty("marker")
	public String marker;
	/**
	* The timestamp when the corresponded data is generated for the report (UTC Epoch in ms)
	*/
	@JsonProperty("timestamp")
	public Long timestamp;
	/**
	* The generated unique identifier of the SFU
	*/
	@JsonProperty("sfuId")
	public String sfuId;
	/**
	* The name of the event
	*/
	@JsonProperty("extensionType")
	public String extensionType;
	/**
	* the human readable message of the event
	*/
	@JsonProperty("payload")
	public String payload;


	public static class Builder {

		private SfuExtensionReport result = new SfuExtensionReport();

		public Builder setServiceId(String value) { this.result.serviceId = value; return this; }
		public Builder setMediaUnitId(String value) { this.result.mediaUnitId = value; return this; }
		public Builder setMarker(String value) { this.result.marker = value; return this; }
		public Builder setTimestamp(Long value) { this.result.timestamp = value; return this; }
		public Builder setSfuId(String value) { this.result.sfuId = value; return this; }
		public Builder setExtensionType(String value) { this.result.extensionType = value; return this; }
		public Builder setPayload(String value) { this.result.payload = value; return this; }
		public SfuExtensionReport build() {
			return this.result;
		}
	}
}