/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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