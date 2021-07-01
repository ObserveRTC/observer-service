//package org.observertc.webrtc.observer.samples;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.util.UUID;
//
//class MediaTrackIdTest {
//
//    private UUID peerConnectionId = UUID.randomUUID();
//    private Long SSRC = 123456789L;
//
//    @Test
//    public void shouldMakeMediaTrackId() {
//        var mediaTrackId = MediaTrackId.make(this.peerConnectionId, this.SSRC);
//
//        Assertions.assertNotNull(mediaTrackId);
//    }
//
//    @Test
//    public void getPeerConnectionId() {
//        var mediaTrackId = MediaTrackId.make(this.peerConnectionId, this.SSRC);
//
//        Assertions.assertEquals(this.peerConnectionId, mediaTrackId.peerConnectionId);
//    }
//
//    @Test
//    public void getMediaTrackId() {
//        var mediaTrackId = MediaTrackId.make(this.peerConnectionId, this.SSRC);
//
//        Assertions.assertEquals(this.SSRC, mediaTrackId.ssrc);
//    }
//
//    @Test
//    public void equalFromKey_1() {
//        var mediaTrackId_1 = MediaTrackId.make(this.peerConnectionId, this.SSRC);
//        var key = mediaTrackId_1.getKey();
//        var mediaTrackId_2 = MediaTrackId.fromKey(key);
//
//        Assertions.assertEquals(mediaTrackId_1, mediaTrackId_2);
//    }
//
//    @Test
//    public void equalFromKey_2() {
//        var mediaTrackId_1 = MediaTrackId.make(this.peerConnectionId, this.SSRC);
//        var key = MediaTrackId.generateKey(mediaTrackId_1);
//        var mediaTrackId_2 = MediaTrackId.fromKey(key);
//
//        Assertions.assertEquals(mediaTrackId_1, mediaTrackId_2);
//    }
//}