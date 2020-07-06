package org.observertc.webrtc.observer.service.repositories;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JooqBulkExecutor<T> {

	private static final Logger logger = LoggerFactory.getLogger(JooqBulkExecutor.class);


	/**
	 * Due to the fact that batch operations increase the size of the query,
	 * this limit enforces to send the query when it reaches a certain
	 * number of entry.
	 */
	private final IDSLContextProvider contextProvider;
	private final int bulkSize;

	/**
	 * @param contextProvider
	 * @param bulkSize
	 */
	public JooqBulkExecutor(IDSLContextProvider contextProvider, int bulkSize) {
		if (bulkSize < 1) {
			throw new IllegalArgumentException("The number of items should a bulk query contain cannot be less than 1");
		}

		if (contextProvider == null) {
			throw new NullPointerException("contextProvider cannot be null");
		}
		this.bulkSize = bulkSize;
		this.contextProvider = contextProvider;
	}

	public boolean execute(BiConsumer<DSLContext, Iterable<T>> processor, Iterable<T> items) {
		return this.execute(processor, items, this.bulkSize);
	}

	private boolean execute(BiConsumer<DSLContext, Iterable<T>> processor, Iterable<T> items, int batchSize) {
		try {
			this.contextProvider.get().transaction(configuration -> {
				List<T> entities = new LinkedList<>();
				for (Iterator<T> it = items.iterator(); it.hasNext(); ) {
					T item = it.next();
					entities.add(item);
					if (batchSize < entities.size()) {
						processor.accept(configuration.dsl(), entities);
						entities.clear();
					}
				}

				if (!entities.isEmpty()) {
					processor.accept(configuration.dsl(), entities);
				}
			});
			return true;
		} catch (DataAccessException ex) {
			logger.warn("Exception related to database occurred", ex);
		} catch (Exception ex) {
			logger.error("Exception happened during execution", ex);
		}
		return false;
	}


}