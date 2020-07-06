package org.observertc.webrtc.observer.service.repositories;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JooqBulkWrapper {

	private static final Logger logger = LoggerFactory.getLogger(JooqBulkWrapper.class);


	/**
	 * Due to the fact that batch operations increase the size of the query,
	 * this limit enforces to send the query when it reaches a certain
	 * number of entry.
	 */
	private final IDSLContextProvider contextProvider;
	private final int defaultBulkSize;

	/**
	 * @param contextProvider
	 * @param defaultBulkSize
	 */
	public JooqBulkWrapper(IDSLContextProvider contextProvider, int defaultBulkSize) {
		if (defaultBulkSize < 1) {
			throw new IllegalArgumentException("The number of items should a bulk query contain cannot be less than 1");
		}

		if (contextProvider == null) {
			throw new NullPointerException("contextProvider cannot be null");
		}
		this.defaultBulkSize = defaultBulkSize;
		this.contextProvider = contextProvider;
	}

	public <E> Iterator<E> retrieve(BiFunction<DSLContext, Integer, Iterator<E>> seekProcessor, int totalSize) {
		try {

			return new Iterator<E>() {
				private AtomicInteger index = new AtomicInteger(0);
				private Iterator<E> entitiesIterator = seekProcessor.apply(contextProvider.get(), // the context we are
						0 // offset
				);

				@Override
				public boolean hasNext() {
					return index.get() < totalSize;
				}

				@Override
				public E next() {
					if (this.entitiesIterator.hasNext()) {
						index.incrementAndGet();
						return this.entitiesIterator.next();
					}

					// we do not have anything in the current stream, so let's load again
					this.entitiesIterator = seekProcessor.apply(contextProvider.get(),
							index.get()
					);
					index.incrementAndGet();
					return this.entitiesIterator.next();
				}
			};
		} catch (Exception ex) {
			logger.error("Exception happened during execution", ex);
		}
		return null;
	}

	public <T> boolean execute(BiConsumer<DSLContext, Iterable<T>> processor, Iterable<T> items) {
		return this.execute(processor, items, this.defaultBulkSize);
	}

	public <T> boolean execute(BiConsumer<DSLContext, Iterable<T>> processor, Iterable<T> items, int batchSize) {
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