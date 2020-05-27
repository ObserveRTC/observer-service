package com.observertc.gatekeeper.builders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.constructor.SafeConstructor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationLoader implements IConfigurationLoader {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

	private final Map<String, IConfigurationProfiles> configurationSources = new HashMap<>();
	private final boolean createOnRead;
	private List<String> packageResolvers = new LinkedList<>();


	public ConfigurationLoader() {
		this(true);
	}

	/**
	 * @param createOnRead determines if it creates new {@link IConfigurationProfiles}
	 *                     while reading sources or not.
	 */
	public ConfigurationLoader(boolean createOnRead) {
		this.createOnRead = createOnRead;
	}

	public ConfigurationLoader forSource(String sourceKey, IConfigurationProfiles configurationSource) {
		this.configurationSources.put(sourceKey, configurationSource);
		return this;
	}

	@Override
	public Map<String, IConfigurationProfiles> getConfigurationSources() {
		return this.configurationSources;
	}

	public IConfigurationProfiles getConfigurationSourceFor(String sourceKey) {
		return this.configurationSources.get(sourceKey);
	}

	public IConfigurationLoader withYaml(String yamlString) throws JsonProcessingException {
		YAMLFactory yamlFactory = new YAMLFactory();
		YAMLMapper mapper = new YAMLMapper(yamlFactory);
		Map<String, Object> input = mapper.readValue(yamlString, Map.class);
		this.evaluate(input);
		return this;
	}

	public IConfigurationLoader withYaml(File file) throws IOException {
		try (InputStream input = new FileInputStream(file)) {
			return this.withYaml(input);
		} catch (Throwable e) {
			logger.warn("Error occured at parsing process.", e);
			throw e;
		}
	}

	public IConfigurationLoader withYaml(InputStream input) {
		Map<String, Object> configs = null;
		Yaml yaml = new Yaml(new SafeConstructor());
		Iterable<Object> iterable = yaml.loadAll(input);
		for (Iterator<Object> it = iterable.iterator(); it.hasNext(); ) {
			Object obj = it.next();
			if (obj instanceof Map == false) {
				continue;
			}
			configs = (Map<String, Object>) obj;
			if (configs == null) {
				logger.debug("It does not contain any configuration");
				return this;
			}
			this.evaluate(configs);
		}
		return this;
	}

	public IConfigurationLoader withJSON(String jsonString) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(
				jsonString, new TypeReference<Map<String, Object>>() {
				});
		this.evaluate(map);
		return this;
	}

	public IConfigurationLoader withJSON(File file) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(
				file, new TypeReference<Map<String, Object>>() {
				});
		this.evaluate(map);
		return this;
	}

	public ConfigurationLoader withProfiles(String sourceKey, IConfigurationProfiles profiles) {
		this.configurationSources.put(sourceKey, profiles);
		return this;
	}


	public IConfigurationLoader mergeWith(IConfigurationLoader peer) {
		AbstractBuilder.deepMerge(this.configurationSources, peer.getConfigurationSources());
		return this;
	}

	public List<String> getPackageResolvers() {
		return packageResolvers;
	}

	@Override
	public IConfigurationLoader withPackageResolvers(List<String> packageResolvers) {
		this.packageResolvers.addAll(packageResolvers);
		return this;
	}

	private void evaluate(Map<String, Object> loadedConfigs) {
		for (Iterator<Map.Entry<String, Object>> it = loadedConfigs.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, Object> entry = it.next();
			String sourceKey = entry.getKey();
			if (entry.getValue() instanceof Map == false) {
				logger.debug("Found a non map profiles");
				continue;
			}
			Map<String, Object> source = (Map<String, Object>) entry.getValue();
			this.evaluate(sourceKey, source);
		}
	}

	private void evaluate(String sourceKey, Map<String, Object> source) {
		IConfigurationProfiles configurationProfiles = this.configurationSources.get(sourceKey);
		if (configurationProfiles == null) {
			if (!this.createOnRead) {
				logger.debug("An unrecognized profilekey in ConfigurationLoader: " + sourceKey);
				return;
			}
			configurationProfiles = new ConfigurationProfiles().using(this);
			this.configurationSources.put(sourceKey, configurationProfiles);
		}

		configurationProfiles.withProfiles(source);
	}


}
