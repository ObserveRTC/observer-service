package org.observertc.webrtc.observer.service.builders;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * An interface for a loader holds configuration from different sources
 */
public interface IConfigurationLoader {

	Map<String, IConfigurationProfiles> getConfigurationSources();

	IConfigurationProfiles getConfigurationSourceFor(String sourceKey);

	IConfigurationLoader withYaml(InputStream input);

	IConfigurationLoader withProfiles(String sourceKey, IConfigurationProfiles profiles);

	IConfigurationLoader mergeWith(IConfigurationLoader peer);

	IConfigurationLoader withPackageResolvers(List<String> packageResolvers);

	List<String> getPackageResolvers();
}
