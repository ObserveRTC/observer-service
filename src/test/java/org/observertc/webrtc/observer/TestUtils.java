package org.observertc.webrtc.observer;

import java.util.List;

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

    public static List<String> getTestRoomIds() {
        return List.of(
                "Kickstart Meetings",
                "Leaders Think Space",
                "Banding Together",
                "Fellowship Hall"
        );
    }

    public static List<String> getLabels() {
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
}
