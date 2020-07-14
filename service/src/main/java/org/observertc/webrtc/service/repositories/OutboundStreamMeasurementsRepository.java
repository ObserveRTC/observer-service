package org.observertc.webrtc.service.repositories;

import static org.jooq.impl.DSL.row;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.repository.CrudRepository;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.javatuples.Triplet;
import org.jooq.Field;
import org.jooq.InsertValuesStepN;
import org.jooq.Row3;
import org.jooq.impl.DSL;
import org.observertc.webrtc.service.UUIDAdapter;
import org.observertc.webrtc.service.dto.OutboundStreamMeasurementDTO;
import org.observertc.webrtc.service.jooq.Tables;
import org.observertc.webrtc.service.jooq.tables.Outboundstreammeasurements;
import org.observertc.webrtc.service.jooq.tables.records.OutboundstreammeasurementsRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OutboundStreamMeasurementsRepository implements CrudRepository<OutboundStreamMeasurementDTO, Triplet<UUID, UUID, Long>> {

	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionSSRCsRepository.class);
	private static final Outboundstreammeasurements TABLE = Tables.OUTBOUNDSTREAMMEASUREMENTS;

	private static final int BULK_QUERY_MAX_ITEMS = 5000;

	/**
	 * For insert into set
	 *
	 * @param field
	 * @param <T>
	 * @return
	 */
	private static <T> Field<T> values(Field<T> field) {
		return DSL.field("VALUES({0})", field.getDataType(), field);
	}

	/**
	 * Due to the fact that bulk operations increase the size of the query,
	 * this limit enforces to send the query when it reaches a certain
	 * number of entry.
	 */
	private final IDSLContextProvider contextProvider;

	public OutboundStreamMeasurementsRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
	}

	@NonNull
	@Override
	public <S extends OutboundStreamMeasurementDTO> S save(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(TABLE)
				.columns(TABLE.OBSERVERUUID,
						TABLE.PEERCONNECTIONUUID,
						TABLE.SSRC,
						TABLE.FIRSTSAMPLE,
						TABLE.LASTSAMPLE,
						TABLE.REPORTED,
						TABLE.SAMPLES_COUNT,

						TABLE.BYTESSENT_COUNT,
						TABLE.BYTESSENT_SUM,
						TABLE.BYTESSENT_MIN,
						TABLE.BYTESSENT_MAX,
						TABLE.BYTESSENT_LAST,

						TABLE.PACKETSSENT_COUNT,
						TABLE.PACKETSSENT_SUM,
						TABLE.PACKETSSENT_MIN,
						TABLE.PACKETSSENT_MAX,
						TABLE.PACKETSSENT_LAST,

						TABLE.ENCODED_FRAMES_COUNT,
						TABLE.QPSUM_COUNT,
						TABLE.QPSUM_SUM,
						TABLE.QPSUM_MIN,
						TABLE.QPSUM_MAX,
						TABLE.QPSUM_LAST

				)
				.values(
						UUIDAdapter.toBytes(entity.observerUUID),
						UUIDAdapter.toBytes(entity.peerConnectionUUID),
						entity.SSRC,
						entity.firstSample,
						entity.lastSample,
						entity.reported,
						entity.samples_count,

						entity.bytesSent_count,
						entity.bytesSent_sum,
						entity.bytesSent_min,
						entity.bytesSent_max,
						entity.bytesSent_last,

						entity.packetsSent_count,
						entity.packetsSent_sum,
						entity.packetsSent_min,
						entity.packetsSent_max,
						entity.packetsSent_last,

						entity.encoded_frames_count,
						entity.qpSum_count,
						entity.qpSum_sum,
						entity.qpSum_min,
						entity.qpSum_max,
						entity.qpSum_last

				)
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends OutboundStreamMeasurementDTO> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(TABLE)
				.columns(TABLE.OBSERVERUUID,
						TABLE.PEERCONNECTIONUUID,
						TABLE.SSRC,
						TABLE.FIRSTSAMPLE,
						TABLE.LASTSAMPLE,
						TABLE.REPORTED,
						TABLE.SAMPLES_COUNT,

						TABLE.BYTESSENT_COUNT,
						TABLE.BYTESSENT_SUM,
						TABLE.BYTESSENT_MIN,
						TABLE.BYTESSENT_MAX,
						TABLE.BYTESSENT_LAST,

						TABLE.PACKETSSENT_COUNT,
						TABLE.PACKETSSENT_SUM,
						TABLE.PACKETSSENT_MIN,
						TABLE.PACKETSSENT_MAX,
						TABLE.PACKETSSENT_LAST,

						TABLE.ENCODED_FRAMES_COUNT,
						TABLE.QPSUM_COUNT,
						TABLE.QPSUM_SUM,
						TABLE.QPSUM_MIN,
						TABLE.QPSUM_MAX,
						TABLE.QPSUM_LAST

				)
				.values(
						UUIDAdapter.toBytes(entity.observerUUID),
						UUIDAdapter.toBytes(entity.peerConnectionUUID),
						entity.SSRC,
						entity.firstSample,
						entity.lastSample,
						entity.reported,
						entity.samples_count,

						entity.bytesSent_count,
						entity.bytesSent_sum,
						entity.bytesSent_min,
						entity.bytesSent_max,
						entity.bytesSent_last,

						entity.packetsSent_count,
						entity.packetsSent_sum,
						entity.packetsSent_min,
						entity.packetsSent_max,
						entity.packetsSent_last,

						entity.encoded_frames_count,
						entity.qpSum_count,
						entity.qpSum_sum,
						entity.qpSum_min,
						entity.qpSum_max,
						entity.qpSum_last
				)
				.onDuplicateKeyUpdate()
				.set(TABLE.OBSERVERUUID, UUIDAdapter.toBytes(entity.observerUUID))
				.set(TABLE.PEERCONNECTIONUUID, UUIDAdapter.toBytes(entity.peerConnectionUUID))
				.set(TABLE.SSRC, entity.SSRC)
				.set(TABLE.FIRSTSAMPLE, entity.firstSample)
				.set(TABLE.LASTSAMPLE, entity.lastSample)
				.set(TABLE.REPORTED, entity.reported)
				.set(TABLE.SAMPLES_COUNT, entity.samples_count)

				.set(TABLE.BYTESSENT_COUNT, entity.bytesSent_count)
				.set(TABLE.BYTESSENT_SUM, entity.bytesSent_sum)
				.set(TABLE.BYTESSENT_MIN, entity.bytesSent_min)
				.set(TABLE.BYTESSENT_MAX, entity.bytesSent_max)
				.set(TABLE.BYTESSENT_LAST, entity.bytesSent_last)

				.set(TABLE.PACKETSSENT_COUNT, entity.packetsSent_count)
				.set(TABLE.PACKETSSENT_SUM, entity.packetsSent_sum)
				.set(TABLE.PACKETSSENT_MIN, entity.packetsSent_min)
				.set(TABLE.PACKETSSENT_MAX, entity.packetsSent_max)
				.set(TABLE.PACKETSSENT_LAST, entity.packetsSent_last)

				.set(TABLE.QPSUM_COUNT, entity.qpSum_count)
				.set(TABLE.QPSUM_SUM, entity.qpSum_sum)
				.set(TABLE.QPSUM_MIN, entity.qpSum_min)
				.set(TABLE.QPSUM_MAX, entity.qpSum_max)
				.set(TABLE.QPSUM_LAST, entity.qpSum_last)

				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends OutboundStreamMeasurementDTO> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		Supplier<InsertValuesStepN<OutboundstreammeasurementsRecord>> sqlSupplier = () -> {
			return this.contextProvider.get()
					.insertInto(TABLE)
					.columns(TABLE.OBSERVERUUID,
							TABLE.PEERCONNECTIONUUID,
							TABLE.SSRC,
							TABLE.FIRSTSAMPLE,
							TABLE.LASTSAMPLE,
							TABLE.REPORTED,
							TABLE.SAMPLES_COUNT,

							TABLE.BYTESSENT_COUNT,
							TABLE.BYTESSENT_SUM,
							TABLE.BYTESSENT_MIN,
							TABLE.BYTESSENT_MAX,
							TABLE.BYTESSENT_LAST,

							TABLE.PACKETSSENT_COUNT,
							TABLE.PACKETSSENT_SUM,
							TABLE.PACKETSSENT_MIN,
							TABLE.PACKETSSENT_MAX,
							TABLE.PACKETSSENT_LAST,

							TABLE.ENCODED_FRAMES_COUNT,
							TABLE.QPSUM_COUNT,
							TABLE.QPSUM_SUM,
							TABLE.QPSUM_MIN,
							TABLE.QPSUM_MAX,
							TABLE.QPSUM_LAST

					);
		};
		Consumer<InsertValuesStepN<OutboundstreammeasurementsRecord>> executor = (sql) -> {
			sql.onDuplicateKeyUpdate()

					.set(TABLE.OBSERVERUUID, values(TABLE.OBSERVERUUID))
					.set(TABLE.PEERCONNECTIONUUID, values(TABLE.PEERCONNECTIONUUID))
					.set(TABLE.SSRC, values(TABLE.SSRC))
					.set(TABLE.FIRSTSAMPLE, values(TABLE.FIRSTSAMPLE))
					.set(TABLE.LASTSAMPLE, values(TABLE.LASTSAMPLE))
					.set(TABLE.REPORTED, values(TABLE.REPORTED))
					.set(TABLE.SAMPLES_COUNT, values(TABLE.SAMPLES_COUNT))

					.set(TABLE.BYTESSENT_COUNT, values(TABLE.BYTESSENT_COUNT))
					.set(TABLE.BYTESSENT_SUM, values(TABLE.BYTESSENT_SUM))
					.set(TABLE.BYTESSENT_MIN, values(TABLE.BYTESSENT_MIN))
					.set(TABLE.BYTESSENT_MAX, values(TABLE.BYTESSENT_MAX))
					.set(TABLE.BYTESSENT_LAST, values(TABLE.BYTESSENT_LAST))

					.set(TABLE.PACKETSSENT_COUNT, values(TABLE.PACKETSSENT_COUNT))
					.set(TABLE.PACKETSSENT_SUM, values(TABLE.PACKETSSENT_SUM))
					.set(TABLE.PACKETSSENT_MIN, values(TABLE.PACKETSSENT_MIN))
					.set(TABLE.PACKETSSENT_MAX, values(TABLE.PACKETSSENT_MAX))
					.set(TABLE.PACKETSSENT_LAST, values(TABLE.PACKETSSENT_LAST))

					.set(TABLE.QPSUM_COUNT, values(TABLE.QPSUM_COUNT))
					.set(TABLE.QPSUM_SUM, values(TABLE.QPSUM_SUM))
					.set(TABLE.QPSUM_MIN, values(TABLE.QPSUM_MIN))
					.set(TABLE.QPSUM_MAX, values(TABLE.QPSUM_MAX))
					.set(TABLE.QPSUM_LAST, values(TABLE.QPSUM_LAST))
					.execute();
		};
		InsertValuesStepN<OutboundstreammeasurementsRecord> sql = sqlSupplier.get();
		int count = 0;
		for (Iterator<S> it = entities.iterator(); it.hasNext(); ++count) {
			OutboundStreamMeasurementDTO entity = it.next();
			byte[] peerConnection = UUIDAdapter.toBytes(entity.peerConnectionUUID);
			byte[] observer = UUIDAdapter.toBytes(entity.observerUUID);
			sql.values(
					observer,
					peerConnection,
					entity.SSRC,
					entity.firstSample,
					entity.lastSample,
					entity.reported,
					entity.samples_count,

					entity.bytesSent_count,
					entity.bytesSent_sum,
					entity.bytesSent_min,
					entity.bytesSent_max,
					entity.bytesSent_last,

					entity.packetsSent_count,
					entity.packetsSent_sum,
					entity.packetsSent_min,
					entity.packetsSent_max,
					entity.packetsSent_last,

					entity.qpSum_count,
					entity.qpSum_sum,
					entity.qpSum_min,
					entity.qpSum_max,
					entity.qpSum_last

			);
			if (BULK_QUERY_MAX_ITEMS < count) {
				executor.accept(sql);
				sql = sqlSupplier.get();
				count = 0;
			}
		}

		if (0 < count) {
			executor.accept(sql);
		}
		return entities;
	}

	@NonNull
	@Override
	public Optional<OutboundStreamMeasurementDTO> findById(@NonNull @NotNull Triplet<UUID, UUID, Long> observerPeerConnectionSSRC) {
		return this.contextProvider.get()
				.selectFrom(TABLE)
				.where(row(TABLE.OBSERVERUUID, TABLE.PEERCONNECTIONUUID, TABLE.SSRC)
						.eq(UUIDAdapter.toBytes(observerPeerConnectionSSRC.getValue0()),
								UUIDAdapter.toBytes(observerPeerConnectionSSRC.getValue1()),
								observerPeerConnectionSSRC.getValue2()))
				.fetchOptionalInto(OutboundStreamMeasurementDTO.class);
	}


	@Override
	public boolean existsById(@NonNull @NotNull Triplet<UUID, UUID, Long> observerPeerConnectionSSRC) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(TABLE)
						.where(row(TABLE.OBSERVERUUID, TABLE.PEERCONNECTIONUUID, TABLE.SSRC)
								.eq(UUIDAdapter.toBytes(observerPeerConnectionSSRC.getValue0()),
										UUIDAdapter.toBytes(observerPeerConnectionSSRC.getValue1()),
										observerPeerConnectionSSRC.getValue2()))
		);
	}

	@NonNull
	@Override
	public Iterable<OutboundStreamMeasurementDTO> findAll() {
		return () -> this.contextProvider.get()
				.selectFrom(TABLE)
				.fetchInto(OutboundStreamMeasurementDTO.class).iterator();
	}

	@Override
	public long count() {
		return this.contextProvider
				.get()
				.selectCount()
				.from(TABLE)
				.fetchOne(0, long.class);
	}

	@Override
	public void deleteById(@NonNull @NotNull Triplet<UUID, UUID, Long> observerPeerConnectionSSRC) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(row(TABLE.OBSERVERUUID, TABLE.PEERCONNECTIONUUID, TABLE.SSRC)
						.eq(UUIDAdapter.toBytes(observerPeerConnectionSSRC.getValue0()),
								UUIDAdapter.toBytes(observerPeerConnectionSSRC.getValue1()),
								observerPeerConnectionSSRC.getValue2()))
				.execute();

	}

	@Override
	public void delete(@NonNull @NotNull OutboundStreamMeasurementDTO entity) {
		this.contextProvider.get()
				.deleteFrom(Tables.PEERCONNECTIONSSRCS)
				.where(row(TABLE.OBSERVERUUID, TABLE.PEERCONNECTIONUUID, TABLE.SSRC)
						.eq(UUIDAdapter.toBytes(entity.observerUUID),
								UUIDAdapter.toBytes(entity.peerConnectionUUID),
								entity.SSRC))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends OutboundStreamMeasurementDTO> entities) {
		List<Row3<byte[], byte[], Long>> keys = StreamSupport.stream(entities
				.spliterator(), false)
				.map(entity -> row(
						UUIDAdapter.toBytes(entity.observerUUID),
						UUIDAdapter.toBytes(entity.peerConnectionUUID),
						entity.SSRC
				)).collect(Collectors.toList());
		this.contextProvider.get().deleteFrom(TABLE)
				.where(row(TABLE.OBSERVERUUID, TABLE.PEERCONNECTIONUUID, TABLE.SSRC)
						.in(keys)).execute();
	}

	@Override
	public void deleteAll() {
		this.contextProvider.get().deleteFrom(TABLE);
	}

	public Map<Triplet<UUID, UUID, Long>, OutboundStreamMeasurementDTO> findByIds(Iterable<Triplet<UUID, UUID, Long>> keys) {
		List<Row3<byte[], byte[], Long>> mappedKeys = StreamSupport.stream(keys
				.spliterator(), false)
				.map(key -> row(
						UUIDAdapter.toBytes(key.getValue0()),
						UUIDAdapter.toBytes(key.getValue1()),
						key.getValue2()
				)).collect(Collectors.toList());
		return this.contextProvider.get().selectFrom(TABLE)
				.where(row(TABLE.OBSERVERUUID, TABLE.PEERCONNECTIONUUID, TABLE.SSRC)
						.in(mappedKeys))
				.fetchInto(OutboundStreamMeasurementDTO.class)
				.stream()
				.collect(Collectors.toMap(dto -> Triplet.with(dto.observerUUID, dto.peerConnectionUUID, dto.SSRC), Function.identity()));
	}
}