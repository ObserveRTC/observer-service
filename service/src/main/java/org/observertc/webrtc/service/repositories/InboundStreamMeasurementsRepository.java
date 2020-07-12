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
import org.observertc.webrtc.service.dto.InboundStreamMeasurementDTO;
import org.observertc.webrtc.service.jooq.Tables;
import org.observertc.webrtc.service.jooq.tables.Inboundstreammeasurements;
import org.observertc.webrtc.service.jooq.tables.records.InboundstreammeasurementsRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InboundStreamMeasurementsRepository implements CrudRepository<InboundStreamMeasurementDTO, Triplet<UUID, UUID, Long>> {

	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionSSRCsRepository.class);
	private static final Inboundstreammeasurements TABLE = Tables.INBOUNDSTREAMMEASUREMENTS;

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

	public InboundStreamMeasurementsRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
	}

	@NonNull
	@Override
	public <S extends InboundStreamMeasurementDTO> S save(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(TABLE)
				.columns(TABLE.OBSERVERUUID,
						TABLE.PEERCONNECTIONUUID,
						TABLE.SSRC,
						TABLE.FIRSTSAMPLE,
						TABLE.LASTSAMPLE,
						TABLE.REPORTED,
						TABLE.SAMPLES_COUNT,

						TABLE.BYTESRECEIVED_COUNT,
						TABLE.BYTESRECEIVED_SUM,
						TABLE.BYTESRECEIVED_MIN,
						TABLE.BYTESRECEIVED_MAX,
						TABLE.BYTESRECEIVED_LAST,

						TABLE.PACKETSRECEIVED_COUNT,
						TABLE.PACKETSRECEIVED_SUM,
						TABLE.PACKETSRECEIVED_MIN,
						TABLE.PACKETSRECEIVED_MAX,
						TABLE.PACKETSRECEIVED_LAST,

						TABLE.PACKETSLOST_COUNT,
						TABLE.PACKETSLOST_SUM,
						TABLE.PACKETSLOST_MIN,
						TABLE.PACKETSLOST_MAX,
						TABLE.PACKETSLOST_LAST,

						TABLE.DECODED_FRAMES_COUNT,
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

						entity.bytesReceived_count,
						entity.bytesReceived_sum,
						entity.bytesReceived_min,
						entity.bytesReceived_max,
						entity.bytesReceived_last,

						entity.packetsReceived_count,
						entity.packetsReceived_sum,
						entity.packetsReceived_min,
						entity.packetsReceived_max,
						entity.packetsReceived_last,

						entity.packetsLost_count,
						entity.packetsLost_sum,
						entity.packetsLost_min,
						entity.packetsLost_max,
						entity.packetsLost_last,

						entity.decoded_frames_count,
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
	public <S extends InboundStreamMeasurementDTO> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(TABLE)
				.columns(TABLE.OBSERVERUUID,
						TABLE.PEERCONNECTIONUUID,
						TABLE.SSRC,
						TABLE.FIRSTSAMPLE,
						TABLE.LASTSAMPLE,
						TABLE.REPORTED,
						TABLE.SAMPLES_COUNT,

						TABLE.BYTESRECEIVED_COUNT,
						TABLE.BYTESRECEIVED_SUM,
						TABLE.BYTESRECEIVED_MIN,
						TABLE.BYTESRECEIVED_MAX,
						TABLE.BYTESRECEIVED_LAST,

						TABLE.PACKETSRECEIVED_COUNT,
						TABLE.PACKETSRECEIVED_SUM,
						TABLE.PACKETSRECEIVED_MIN,
						TABLE.PACKETSRECEIVED_MAX,
						TABLE.PACKETSRECEIVED_LAST,

						TABLE.PACKETSLOST_COUNT,
						TABLE.PACKETSLOST_SUM,
						TABLE.PACKETSLOST_MIN,
						TABLE.PACKETSLOST_MAX,
						TABLE.PACKETSLOST_LAST,

						TABLE.DECODED_FRAMES_COUNT,
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

						entity.bytesReceived_count,
						entity.bytesReceived_sum,
						entity.bytesReceived_min,
						entity.bytesReceived_max,
						entity.bytesReceived_last,

						entity.packetsReceived_count,
						entity.packetsReceived_sum,
						entity.packetsReceived_min,
						entity.packetsReceived_max,
						entity.packetsReceived_last,

						entity.packetsLost_count,
						entity.packetsLost_sum,
						entity.packetsLost_min,
						entity.packetsLost_max,
						entity.packetsLost_last,

						entity.decoded_frames_count,
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

				.set(TABLE.BYTESRECEIVED_COUNT, entity.bytesReceived_count)
				.set(TABLE.BYTESRECEIVED_SUM, entity.bytesReceived_sum)
				.set(TABLE.BYTESRECEIVED_MIN, entity.bytesReceived_min)
				.set(TABLE.BYTESRECEIVED_MAX, entity.bytesReceived_max)
				.set(TABLE.BYTESRECEIVED_LAST, entity.bytesReceived_last)

				.set(TABLE.PACKETSRECEIVED_COUNT, entity.packetsReceived_count)
				.set(TABLE.PACKETSRECEIVED_SUM, entity.packetsReceived_sum)
				.set(TABLE.PACKETSRECEIVED_MIN, entity.packetsReceived_min)
				.set(TABLE.PACKETSRECEIVED_MAX, entity.packetsReceived_max)
				.set(TABLE.PACKETSRECEIVED_LAST, entity.packetsReceived_last)

				.set(TABLE.PACKETSLOST_COUNT, entity.packetsLost_count)
				.set(TABLE.PACKETSLOST_SUM, entity.packetsLost_sum)
				.set(TABLE.PACKETSLOST_MIN, entity.packetsLost_min)
				.set(TABLE.PACKETSLOST_MAX, entity.packetsLost_max)
				.set(TABLE.PACKETSLOST_LAST, entity.packetsLost_last)

				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends InboundStreamMeasurementDTO> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		Supplier<InsertValuesStepN<InboundstreammeasurementsRecord>> sqlSupplier = () -> {
			return this.contextProvider.get()
					.insertInto(TABLE)
					.columns(TABLE.OBSERVERUUID,
							TABLE.PEERCONNECTIONUUID,
							TABLE.SSRC,
							TABLE.FIRSTSAMPLE,
							TABLE.LASTSAMPLE,
							TABLE.REPORTED,
							TABLE.SAMPLES_COUNT,

							TABLE.BYTESRECEIVED_COUNT,
							TABLE.BYTESRECEIVED_SUM,
							TABLE.BYTESRECEIVED_MIN,
							TABLE.BYTESRECEIVED_MAX,
							TABLE.BYTESRECEIVED_LAST,

							TABLE.PACKETSRECEIVED_COUNT,
							TABLE.PACKETSRECEIVED_SUM,
							TABLE.PACKETSRECEIVED_MIN,
							TABLE.PACKETSRECEIVED_MAX,
							TABLE.PACKETSRECEIVED_LAST,

							TABLE.PACKETSLOST_COUNT,
							TABLE.PACKETSLOST_SUM,
							TABLE.PACKETSLOST_MIN,
							TABLE.PACKETSLOST_MAX,
							TABLE.PACKETSLOST_LAST,

							TABLE.DECODED_FRAMES_COUNT,
							TABLE.QPSUM_COUNT,
							TABLE.QPSUM_SUM,
							TABLE.QPSUM_MIN,
							TABLE.QPSUM_MAX,
							TABLE.QPSUM_LAST

					);
		};
		Consumer<InsertValuesStepN<InboundstreammeasurementsRecord>> executor = (sql) -> {
			sql.onDuplicateKeyUpdate()
					.set(TABLE.SSRC, values(TABLE.SSRC))
					.set(TABLE.FIRSTSAMPLE, values(TABLE.FIRSTSAMPLE))
					.set(TABLE.LASTSAMPLE, values(TABLE.LASTSAMPLE))
					.set(TABLE.REPORTED, values(TABLE.REPORTED))
					.set(TABLE.SAMPLES_COUNT, values(TABLE.SAMPLES_COUNT))

					.set(TABLE.BYTESRECEIVED_COUNT, values(TABLE.BYTESRECEIVED_COUNT))
					.set(TABLE.BYTESRECEIVED_SUM, values(TABLE.BYTESRECEIVED_SUM))
					.set(TABLE.BYTESRECEIVED_MIN, values(TABLE.BYTESRECEIVED_MIN))
					.set(TABLE.BYTESRECEIVED_MAX, values(TABLE.BYTESRECEIVED_MAX))
					.set(TABLE.BYTESRECEIVED_LAST, values(TABLE.BYTESRECEIVED_LAST))

					.set(TABLE.PACKETSRECEIVED_COUNT, values(TABLE.PACKETSRECEIVED_COUNT))
					.set(TABLE.PACKETSRECEIVED_SUM, values(TABLE.PACKETSRECEIVED_SUM))
					.set(TABLE.PACKETSRECEIVED_MIN, values(TABLE.PACKETSRECEIVED_MIN))
					.set(TABLE.PACKETSRECEIVED_MAX, values(TABLE.PACKETSRECEIVED_MAX))
					.set(TABLE.PACKETSRECEIVED_LAST, values(TABLE.PACKETSRECEIVED_LAST))

					.set(TABLE.QPSUM_COUNT, values(TABLE.QPSUM_COUNT))
					.set(TABLE.QPSUM_SUM, values(TABLE.QPSUM_SUM))
					.set(TABLE.QPSUM_MIN, values(TABLE.QPSUM_MIN))
					.set(TABLE.QPSUM_MAX, values(TABLE.QPSUM_MAX))
					.set(TABLE.QPSUM_LAST, values(TABLE.QPSUM_LAST))
					.execute();
		};
		InsertValuesStepN<InboundstreammeasurementsRecord> sql = sqlSupplier.get();
		int count = 0;
		for (Iterator<S> it = entities.iterator(); it.hasNext(); ++count) {
			InboundStreamMeasurementDTO entity = it.next();
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

					entity.bytesReceived_count,
					entity.bytesReceived_sum,
					entity.bytesReceived_min,
					entity.bytesReceived_max,
					entity.bytesReceived_last,

					entity.packetsReceived_count,
					entity.packetsReceived_sum,
					entity.packetsReceived_min,
					entity.packetsReceived_max,
					entity.packetsReceived_last,

					entity.packetsLost_count,
					entity.packetsLost_sum,
					entity.packetsLost_min,
					entity.packetsLost_max,
					entity.packetsLost_last,

					entity.decoded_frames_count,
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
	public Optional<InboundStreamMeasurementDTO> findById(@NonNull @NotNull Triplet<UUID, UUID, Long> observerPeerConnectionSSRC) {
		return this.contextProvider.get()
				.selectFrom(TABLE)
				.where(row(TABLE.OBSERVERUUID, TABLE.PEERCONNECTIONUUID, TABLE.SSRC)
						.eq(UUIDAdapter.toBytes(observerPeerConnectionSSRC.getValue0()),
								UUIDAdapter.toBytes(observerPeerConnectionSSRC.getValue1()),
								observerPeerConnectionSSRC.getValue2()))
				.fetchOptionalInto(InboundStreamMeasurementDTO.class);
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
	public Iterable<InboundStreamMeasurementDTO> findAll() {
		return () -> this.contextProvider.get()
				.selectFrom(TABLE)
				.fetchInto(InboundStreamMeasurementDTO.class).iterator();
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
	public void delete(@NonNull @NotNull InboundStreamMeasurementDTO entity) {
		this.contextProvider.get()
				.deleteFrom(Tables.PEERCONNECTIONSSRCS)
				.where(row(TABLE.OBSERVERUUID, TABLE.PEERCONNECTIONUUID, TABLE.SSRC)
						.eq(UUIDAdapter.toBytes(entity.observerUUID),
								UUIDAdapter.toBytes(entity.peerConnectionUUID),
								entity.SSRC))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends InboundStreamMeasurementDTO> entities) {
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

	public Map<Triplet<UUID, UUID, Long>, InboundStreamMeasurementDTO> findByIds(Iterable<Triplet<UUID, UUID, Long>> keys) {
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
				.fetchInto(InboundStreamMeasurementDTO.class)
				.stream()
				.collect(Collectors.toMap(dto -> Triplet.with(dto.observerUUID, dto.peerConnectionUUID, dto.SSRC), Function.identity()));
	}
}