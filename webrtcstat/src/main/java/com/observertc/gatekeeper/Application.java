package com.observertc.gatekeeper;

import com.observertc.gatekeeper.builders.ConfigurationLoader;
import com.observertc.gatekeeper.builders.IConfigurationLoader;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

public class Application {

	public static ApplicationContext context;

	public static void main(String[] args) {

		IConfigurationLoader configurationLoader = new ConfigurationLoader();
		context = Micronaut.run(Application.class);
	}
}