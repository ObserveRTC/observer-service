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

package org.observertc.webrtc.observer.repositories.mysql;

import static org.jooq.impl.DSL.row;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.jooq.Field;
import org.jooq.InsertValuesStep3;
import org.jooq.impl.DSL;
import org.observertc.webrtc.common.BatchCollector;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.observer.jooq.Tables;
import org.observertc.webrtc.observer.jooq.tables.Activestreams;
import org.observertc.webrtc.observer.jooq.tables.records.ActivestreamsRecord;

@Singleton
public class ActiveStreamsRepository {
	private static Activestreams TABLE = Tables.ACTIVESTREAMS;
	private static int DEFAULT_BULK_SIZE = 5000;

	private static <T> Field<T> values(Field<T> field) {
		return DSL.field("VALUES({0})", field.getDataType(), field);
	}

	private final IDSLContextProvider contextProvider;

	public ActiveStreamsRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
	}

	@NonNull
	public <S extends ActivestreamsRecord> S save(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.execute();
		return entity;
	}

	@NonNull
	public <S extends ActivestreamsRecord> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.onDuplicateKeyUpdate()
				.set(TABLE.SERVICEUUID, values(TABLE.SERVICEUUID))
				.set(TABLE.SSRC, values(TABLE.SSRC))
				.set(TABLE.CALLUUID, values(TABLE.CALLUUID))
				.execute();
		return entity;
	}

	@NonNull
	public <S extends ActivestreamsRecord> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(DEFAULT_BULK_SIZE, batchedEntities -> {
			contextProvider.get().batchInsert(batchedEntities).execute();
		}));
		return entities;
	}

	@NonNull
	public <S extends ActivestreamsRecord> Iterable<S> updateAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		this.consumeBatches(entities, batchedEntities -> {
			InsertValuesStep3<ActivestreamsRecord, byte[], Long, byte[]> sql =
					contextProvider.get().insertInto(
							TABLE,
							TABLE.SERVICEUUID,
							TABLE.SSRC,
							TABLE.CALLUUID);
			Iterator<S> it = batchedEntities.iterator();
			for (; it.hasNext(); ) {
				ActivestreamsRecord record = it.next();
				sql.values(record.getServiceuuid(),
						record.getSsrc(),
						record.getCalluuid());
			}
			sql
					.onDuplicateKeyUpdate()
					.set(TABLE.SERVICEUUID, values(TABLE.SERVICEUUID))
					.set(TABLE.SSRC, values(TABLE.SSRC))
					.set(TABLE.CALLUUID, values(TABLE.CALLUUID))
					.execute();
		});
		return entities;
	}

	private <S extends ActivestreamsRecord> void consumeBatches(Iterable<S> entities, Consumer<Iterable<S>> consumer) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(DEFAULT_BULK_SIZE, batchedEntities -> {
			if (batchedEntities.size() < 1) {
				return;
			}
			consumer.accept(batchedEntities);
		}));
	}


	@NonNull
	public Optional<ActivestreamsRecord> findById(@NonNull @NotNull ActiveStreamKey activeStreamKey) {
		return this.contextProvider.get()
				.selectFrom(TABLE)
				.where(TABLE.SERVICEUUID.eq(activeStreamKey.getServiceUUIDBytes()))
				.and(TABLE.SSRC.eq(activeStreamKey.getSSRC()))
				.fetchOptionalInto(ActivestreamsRecord.class);
	}

	@NonNull
	public Stream<ActivestreamsRecord> streamByIds(@NonNull @NotNull Stream<ActiveStreamKey> activeStreamKeys) {
		return this.contextProvider.get()
				.selectFrom(TABLE)
				.where(row(TABLE.SERVICEUUID, TABLE.SSRC).in(
						activeStreamKeys.map(key -> row(key.getServiceUUIDBytes(), key.getSSRC()))
								.collect(Collectors.toList())
				)).stream();
	}

	public boolean existsById(@NonNull @NotNull ActiveStreamKey activeStreamKey) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(TABLE)
						.where(TABLE.SERVICEUUID.eq(activeStreamKey.getServiceUUIDBytes()))
						.and(TABLE.SSRC.eq(activeStreamKey.getSSRC()))
		);
	}

	@NonNull
	public Iterable<ActivestreamsRecord> findAll() {
		return () -> contextProvider.get().selectFrom(TABLE).stream().iterator();
	}

	public long count() {
		return this.contextProvider
				.get()
				.selectCount()
				.from(TABLE)
				.fetchOne(0, long.class);
	}

	public void deleteById(@NonNull @NotNull ActiveStreamKey activeStreamKey) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.SERVICEUUID.eq(activeStreamKey.getServiceUUIDBytes()))
				.and(TABLE.SSRC.eq(activeStreamKey.getSSRC()))
				.execute();
	}

	public void delete(@NonNull @NotNull ActivestreamsRecord entity) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.SERVICEUUID.eq(entity.getServiceuuid()))
				.and(TABLE.SSRC.eq(entity.getSsrc()))
				.execute();
	}

	public void deleteByCallUUID(@NonNull @NotNull UUID callUUID) {
		byte[] callUUIDBytes = UUIDAdapter.toBytesOrDefault(callUUID, null);
		this.deleteByCallUUIDBytes(callUUIDBytes);
	}

	public void deleteByCallUUIDBytes(@NonNull @NotNull byte[] callUUIDBytes) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.CALLUUID.eq(callUUIDBytes))
				.execute();
	}

	public void deleteAll(@NonNull @NotNull Iterable<? extends ActivestreamsRecord> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(DEFAULT_BULK_SIZE, batchedEntities -> {
			contextProvider.get().deleteFrom(TABLE)
					.where(row(TABLE.SERVICEUUID, TABLE.SSRC)
							.in(batchedEntities.stream().map(
									entity -> row(entity.getServiceuuid(), entity.getSsrc())
							).collect(Collectors.toList())))
					.execute();
		}));
	}

	public void deleteAll() {
		this.contextProvider.get().deleteFrom(TABLE).execute();
	}

}