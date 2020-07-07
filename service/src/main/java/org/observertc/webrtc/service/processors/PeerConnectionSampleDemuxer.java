package org.observertc.webrtc.service.processors;//package com.observertc.gatekeeper.webrtcstat.processors.samples;
//
//import com.observertc.gatekeeper.webrtcstat.dto.webrtcstats.ReceiverStatElement;
//import com.observertc.gatekeeper.webrtcstat.dto.webrtcstats.ReceiverStatType;
//import com.observertc.gatekeeper.webrtcstat.dto.webrtcstats.SenderStatElement;
//import com.observertc.gatekeeper.webrtcstat.dto.webrtcstats.SenderStatType;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Objects;
//import java.util.UUID;
//import org.apache.kafka.streams.kstream.KStream;
//import org.apache.kafka.streams.kstream.Predicate;
//import org.javatuples.Pair;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class PeerConnectionSampleDemuxer {
//	private static final int REMOTE_CANDIDATES_INDEX = 0;
//	private static final int LOCAL_CANDIDATES_INDEX = 1;
//	private static final int MEDIA_STREAMS_INDEX = 2;
//	private static final int MEDIA_TRACK_INDEX = 3;
//	private static final int MEDIA_SOURCE_INDEX = 4;
//	private static final int ICE_PAIR_CONNECTION_INDEX = 5;
//
//	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionSampleDemuxer.class);
//
//
//	private final KStream<UUID, PeerConnectionSampleItem> source;
//	private final KStream<UUID, PeerConnectionSampleItem>[] branches;
//
//
//	public PeerConnectionSampleDemuxer(KStream<UUID, PeerConnectionSampleItem> source) {
//		this.source = source;
//		Predicate<UUID, PeerConnectionSampleItem>[] predicates = this.getPredicates();
//		this.branches = this.source.branch(predicates);
//	}
//
//	public KStream<UUID, PeerConnectionSampleItem> getRemoteCandidatesBranch() {
//		return this.branches[REMOTE_CANDIDATES_INDEX];
//	}
//
//	public KStream<UUID, PeerConnectionSampleItem> getLocalCandidatesBranch() {
//		return this.branches[LOCAL_CANDIDATES_INDEX];
//	}
//
//	public KStream<UUID, PeerConnectionSampleItem> getMediaStreamsBranch() {
//		return this.branches[MEDIA_STREAMS_INDEX];
//	}
//
//	public KStream<UUID, PeerConnectionSampleItem> getMediaTrackBranch() {
//		return this.branches[MEDIA_TRACK_INDEX];
//	}
//
//	public KStream<UUID, PeerConnectionSampleItem> getMediaSourceBranch() {
//		return this.branches[MEDIA_SOURCE_INDEX];
//	}
//
//	public KStream<UUID, PeerConnectionSampleItem> getICECandidatePairBranch() {
//		return this.branches[ICE_PAIR_CONNECTION_INDEX];
//	}
//
//	private Predicate<UUID, PeerConnectionSampleItem>[] getPredicates() {
//		Object[] result = new Object[6];
//		result[REMOTE_CANDIDATES_INDEX] = this.isRemoteCandidate();
//		result[LOCAL_CANDIDATES_INDEX] = this.isLocalCandidate();
//		result[MEDIA_STREAMS_INDEX] = this.isMediaStream();
//		result[MEDIA_TRACK_INDEX] = this.isMediaTrack();
//		result[MEDIA_SOURCE_INDEX] = this.isMediaSource();
//		result[ICE_PAIR_CONNECTION_INDEX] = this.isICEPeerConnection();
//		return (Predicate<UUID, PeerConnectionSampleItem>[]) result;
//	}
//
//	private Predicate<UUID, PeerConnectionSampleItem> isMediaTrack() {
//		return this.predicateBuilder(
//				Pair.with(SenderStatType.TRACK, ReceiverStatType.TRACK)
//		);
//	}
//
//	private Predicate<UUID, PeerConnectionSampleItem> isICEPeerConnection() {
//		return this.predicateBuilder(
//				Pair.with(SenderStatType.CANDIDATE_PAIR, ReceiverStatType.CANDIDATE_PAIR)
//		);
//	}
//
//	private Predicate<UUID, PeerConnectionSampleItem> isMediaSource() {
//		return this.predicateBuilder(
//				Pair.with(SenderStatType.MEDIA_SOURCE, null)
//		);
//	}
//
//	private Predicate<UUID, PeerConnectionSampleItem> isMediaStream() {
//		return this.predicateBuilder(
//				Pair.with(SenderStatType.OUTBOUND_RTP, ReceiverStatType.INBOUND_RTP),
//				Pair.with(SenderStatType.REMOTE_INBOUND_RTP, null)
//		);
//	}
//
//	private Predicate<UUID, PeerConnectionSampleItem> isRemoteCandidate() {
//		return this.predicateBuilder(Pair.with(SenderStatType.LOCAL_CANDIDATE, ReceiverStatType.LOCAL_CANDIDATE));
//	}
//
//	private Predicate<UUID, PeerConnectionSampleItem> isLocalCandidate() {
//		return this.predicateBuilder(Pair.with(SenderStatType.LOCAL_CANDIDATE, ReceiverStatType.LOCAL_CANDIDATE));
//	}
//
//
//	private Predicate<UUID, PeerConnectionSampleItem> predicateBuilder(Pair<SenderStatType, ReceiverStatType>... statPairs) {
//		List<Predicate<UUID, PeerConnectionSampleItem>> predicateList = new LinkedList<>();
//		for (int i = 0; i < statPairs.length; ++i) {
//			Pair<SenderStatType, ReceiverStatType> statPair = statPairs[i];
//			SenderStatType senderStatType = statPair.getValue0();
//			ReceiverStatType receiverStatType = statPair.getValue1();
//			Predicate<UUID, PeerConnectionSampleItem> item;
//			if (senderStatType != null) {
//				item = this.testForSenderStatType(senderStatType);
//				predicateList.add(item);
//			}
//			if (receiverStatType != null) {
//				item = this.testForReceiverStatType(receiverStatType);
//				predicateList.add(item);
//			}
//		}
//		return new Predicate<UUID, PeerConnectionSampleItem>() {
//			@Override
//			public boolean test(UUID key, PeerConnectionSampleItem value) {
//				for (Predicate<UUID, PeerConnectionSampleItem> predicate : predicateList) {
//					if (!predicate.test(key, value)) {
//						return false;
//					}
//				}
//				return true;
//			}
//		};
//	}
//
//	private Predicate<UUID, PeerConnectionSampleItem> testForSenderStatType(SenderStatType statType) {
//		return new Predicate<UUID, PeerConnectionSampleItem>() {
//			@Override
//			public boolean test(UUID key, PeerConnectionSampleItem peerConnectionSampleItem) {
//				SenderStatElement senderStatElement = peerConnectionSampleItem.senderStatElement;
//				if (Objects.isNull(senderStatElement)) {
//					return true;
//				}
//				return senderStatElement.getType().equals(statType);
//			}
//		};
//	}
//
//	private Predicate<UUID, PeerConnectionSampleItem> testForReceiverStatType(ReceiverStatType statType) {
//		return new Predicate<UUID, PeerConnectionSampleItem>() {
//			@Override
//			public boolean test(UUID key, PeerConnectionSampleItem peerConnectionSampleItem) {
//				ReceiverStatElement receiverStatElement = peerConnectionSampleItem.receiverStatElement;
//				if (Objects.isNull(receiverStatElement)) {
//					return true;
//				}
//				return receiverStatElement.getType().equals(statType);
//			}
//		};
//	}
//}
