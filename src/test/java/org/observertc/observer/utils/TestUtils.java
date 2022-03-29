package org.observertc.observer.utils;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class TestUtils {

    public static List<String> getTestUserIds() {
        return List.of(
                "Alice",
                "Bob",
                "Carol",
                "Carlos",
                "Charlie",
                "Chuck",
                "Craig"
        );
    }

    public static List<String> getServiceIds() {
        return List.of(
                "Skynet",
                "Ingen",
                "Umbrella"
        );
    }

    public static List<String> getTestRoomIds() {
        return List.of(
                "Kickstart Meetings",
                "Leaders Think Space",
                "Banding Together",
                "Fellowship Hall"
        );
    }

    public static List<String> getClientSideMediaUintIds() {
        return List.of(
                "chatApp-1.0.0",
                "chatApp-2.0.0",
                "meetingApp",
                "SuperTeamApp"
        );
    }

    public static List<String> getSFUSideMediaUintIds() {
        return List.of(
                "SFU-eu-west",
                "SFU-us-central",
                "SFU-central-asia",
                "SFU-eu-north"
        );
    }

    public static List<String> getPeerConnectionLabels() {
        return List.of(
                "senderPeerConnection",
                "receiverPeerConnection",
                "sfuPeerConnection"
        );
    }

    public static List<String> getIceRole() {
        return List.of(
                "new",
                "checking",
                "connected",
                "completed",
                "disconnected",
                "failed",
                "closed"
        );
    }

    public static List<String> getDtlsState() {
        return List.of(
                "new",
                "connecting",
                "connected",
                "closed",
                "failed"
        );
    }

    public static List<String> getIceState() {
        return List.of(
                "new",
                "checking",
                "connected",
                "completed",
                "disconnected",
                "failed",
                "closed"
        );
    }

    public static List<String> getDtlsCipher() {
        return List.of(
                "TLS_NULL_WITH_NULL_NULL",
                "TLS_RSA_WITH_NULL_MD5",
                "TLS_RSA_WITH_NULL_SHA",
                "TLS_RSA_EXPORT_WITH_RC4_40_MD5"
        );
    }

    public static List<String> getDataChannelState() {
        return List.of(
                "connecting",
                "open",
                "closing",
                "closed"
        );
    }

    public static List<String> getQualityLimitationReason() {
        return List.of(
                "none",
                "cpu",
                "bandwidth",
                "other"
        );
    }

    public static List<String> getIceUrls() {
        return List.of(
                "http://iceServer.com",
                "http://iceServer2.com",
                "http://myserver.com",
                "http://myturn.com"
        );
    }

    public static List<String> getSrtpCipher() {
        return List.of(
                "SRTP_AES128_CM_HMAC_SHA1_80",
                "SRTP_AES128_CM_HMAC_SHA1_32",
                "SRTP_NULL_HMAC_SHA1_80",
                "SRTP_NULL_HMAC_SHA1_32"
        );
    }

    public static List<String> getCandidatePairState() {
        return List.of(
                "frozen",
                "waiting",
                "in-progress",
                "failed",
                "succeeded"
        );
    }

    public static List<String> getNetworkTransportProtocols() {
        return List.of(
                "TCP",
                "UDP"
        );
    }

    public static List<String> getICECandidateTypes() {
        return List.of(
                "host",
                "srflx",
                "prflx",
                "relay"
        );
    }


    public static List<String> getRelayProtocols() {
        return List.of(
                "UDP",
                "TCP",
                "TLS"
        );
    }

    public static List<String> getBrowserName() {
        return List.of(
                "Microsoft Edge",
                "Chrome",
                "Firefox"
        );
    }

    public static List<String> getOperationSystemName() {
        return List.of(
                "Windows",
                "Ubuntu",
                "MacOs"
        );
    }

    public static List<String> getVersionNumber() {
        return List.of(
                "41.1.35.1",
                "10.14.5",
                "13240.14.5",
                "10.13434.5",
                "10.14.53433",
                "12320.12324.5",
                "1345340.14343.5343",
                "1230.1234.5",
                "10.999999"
        );
    }

    public static List<Integer> getClockRates() {
        return List.of(
                48000,
                90000
        );
    }

    public static final String AUDIO_KIND = "audio";
    public static final String VIDEO_KIND = "video";

    public static List<String> getMediaKinds() {
        return List.of(
                AUDIO_KIND,
                VIDEO_KIND
        );
    }

    public static<T> T[] arrayOrNull(T... candidates) {
        if (Objects.isNull(candidates)) return null;
        List<T> list = new LinkedList<>();
        for (T candidate : candidates) {
            list.add(candidate);
        }
        if (list.size() < 1) return null;
        return (T[]) list.toArray();
    }

    public static<T> T[] arrayOrNullFromList(Class<T> klass, List<T> items) {
        if (Objects.isNull(items)) return null;
        if (items.size() < 1) return null;
        @SuppressWarnings("unchecked")
        final T[] result = (T[]) Array.newInstance(klass, items.size());
        for (int c = items.size(), i = 0; i < c; ++i ) {
            result[i] = items.get(i);
        }
        return result;
    }

    public static<T> T[] arrayOrNullFromQueue(Class<T> klass, Queue<T> items) {
        if (Objects.isNull(items)) return null;
        if (items.size() < 1) return null;
        @SuppressWarnings("unchecked")
        final T[] result = (T[]) Array.newInstance(klass, items.size());
        for (int c = items.size(), i = 0; i < c; ++i ) {
            result[i] = items.poll();
        }
        return result;
    }

    public static List<String> getCodecTypes() {
        return List.of(
                "opus",
                "vp8",
                "vp9"
        );
    }

    public static List<String> getMarkers() {
        return List.of(
                "dev",
                "prod",
                "experimental",
                "canary",
                "beta"
        );
    }
}
