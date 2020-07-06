package org.observertc.webrtc.observer.service;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

public class Application {

	public static ApplicationContext context;

	public static void main(String[] args) {

//		IConfigurationLoader configurationLoader = new ConfigurationLoader();
		context = Micronaut.run(Application.class);
	}
}