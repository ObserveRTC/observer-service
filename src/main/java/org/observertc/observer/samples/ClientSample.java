package org.observertc.observer.samples;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.observertc.schemas.samples.Samples;

/**
 * A compound object holds a set of measurements belonging to a aspecific time
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public  class ClientSample extends Samples.ClientSample {

}