package org.observertc.webrtc.common;

import static java.util.Objects.requireNonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * COPYRIGHT: Anonymous
 * I have found this solution in the article:
 * https://www.thetopsites.net/article/52751541.shtml
 * <p>
 * Collects elements in the stream and calls the supplied batch processor
 * after the configured batch size is reached.
 * <p>
 * In case of a parallel stream, the batch processor may be called with
 * elements less than the batch size.
 * <p>
 * The elements are not kept in memory, and the final result will be an
 * empty list.
 *
 * @param <T> Type of the elements being collected
 */
public class BatchCollector<T> implements Collector<T, List<T>, List<T>> {

	/**
	 * Creates a new batch collector
	 *
	 * @param batchSize      the batch size after which the batchProcessor should be called
	 * @param batchProcessor the batch processor which accepts batches of records to process
	 * @param <T>            the type of elements being processed
	 * @return a batch collector instance
	 */
	public static <T> Collector<T, List<T>, List<T>> makeCollector(int batchSize, Consumer<List<T>> batchProcessor) {
		return new BatchCollector<T>(batchSize, batchProcessor);
	}

	private final int batchSize;
	private final Consumer<List<T>> batchProcessor;


	/**
	 * Constructs the batch collector
	 *
	 * @param batchSize      the batch size after which the batchProcessor should be called
	 * @param batchProcessor the batch processor which accepts batches of records to process
	 */
	BatchCollector(int batchSize, Consumer<List<T>> batchProcessor) {
		batchProcessor = requireNonNull(batchProcessor);

		this.batchSize = batchSize;
		this.batchProcessor = batchProcessor;
	}

	public Supplier<List<T>> supplier() {
		return ArrayList::new;
	}

	public BiConsumer<List<T>, T> accumulator() {
		return (ts, t) -> {
			ts.add(t);
			if (ts.size() >= batchSize) {
				batchProcessor.accept(ts);
				ts.clear();
			}
		};
	}

	public BinaryOperator<List<T>> combiner() {
		return (ts, ots) -> {
			// process each parallel list without checking for batch size
			// avoids adding all elements of one to another
			// can be modified if a strict batching mode is required
			batchProcessor.accept(ts);
			batchProcessor.accept(ots);
			return Collections.emptyList();
		};
	}

	public Function<List<T>, List<T>> finisher() {
		return ts -> {
			batchProcessor.accept(ts);
			return Collections.emptyList();
		};
	}

	public Set<Characteristics> characteristics() {
		return Collections.emptySet();
	}
}