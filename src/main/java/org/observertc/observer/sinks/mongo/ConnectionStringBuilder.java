package org.observertc.observer.sinks.mongo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mongodb.ConnectionString;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;

class ConnectionStringBuilder extends AbstractBuilder {

	private static final Logger logger = LoggerFactory.getLogger(ConnectionStringBuilder.class);

	/**
	 * Build the mongoURI
	 *
	 * @return
	 */
	public ConnectionString build() {
		Config config = this.convertAndValidate(Config.class);
		String credential = null;
		if (config.username == null || config.username.equals("null")) {
			logger.warn("username for mongodb is null");
		} else if (config.password == null || config.password.equals("null")) {
			logger.warn("password for mongodb is null");
		} else {
			credential = String.join(":", config.username, config.password);
		}

		List<String> servers = new ArrayList<>();
		for (String serverUri : config.servers) {
			servers.add(serverUri);
		}

		String options = null;
		if (config.options != null) {
			List<String> list = new LinkedList<>();
			Iterator<Map.Entry<String, Object>> it = config.options.entrySet().iterator();
			for (; it.hasNext(); ) {
				Map.Entry<String, Object> entry = it.next();
				list.add(String.join("=", entry.getKey(), entry.getValue().toString()));
			}
			options = String.join("&", list);
		}
		String uri;
		if (credential != null) {
			uri = String.join("@", credential,
					String.join(",", servers)
			);
		} else {
			uri = String.join(",", servers);
		}


		if (options != null) {
			uri = String.join("/?", uri, options);
		}
		return new ConnectionString("mongodb://" + uri);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Config {

		@NotNull
		public List<String> servers;

		public Map<String, Object> options;

		public String username;

		public String password;
	}
}