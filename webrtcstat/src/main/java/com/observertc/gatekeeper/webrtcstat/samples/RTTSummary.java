package com.observertc.gatekeeper.webrtcstat.samples;

import javax.inject.Singleton;

@Singleton
public class RTTSummary {
	public int count;
	public Double sum = null;
	public Double min;
	public Double max;
}