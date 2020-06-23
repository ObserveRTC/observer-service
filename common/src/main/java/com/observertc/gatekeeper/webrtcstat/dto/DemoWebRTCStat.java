package com.observertc.gatekeeper.webrtcstat.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
		"foo",
		"bar",
		"baz"
})
public class DemoWebRTCStat {

	@JsonProperty("foo")
	private String foo;
	@JsonProperty("bar")
	private Integer bar;
	@JsonProperty("baz")
	private Boolean baz;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("foo")
	public String getFoo() {
		return foo;
	}

	@JsonProperty("foo")
	public void setFoo(String foo) {
		this.foo = foo;
	}

	@JsonProperty("bar")
	public Integer getBar() {
		return bar;
	}

	@JsonProperty("bar")
	public void setBar(Integer bar) {
		this.bar = bar;
	}

	@JsonProperty("baz")
	public Boolean getBaz() {
		return baz;
	}

	@JsonProperty("baz")
	public void setBaz(Boolean baz) {
		this.baz = baz;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
