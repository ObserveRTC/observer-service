package org.observertc.webrtc.observer.configs;

public class ObfuscationsConfig {
    public boolean enabled = false;
    public String hashAlgorithm;
    public String salt;

    public boolean obfuscateIceAddresses = false;
    public boolean obfuscateUserId = false;
    public boolean obfuscateRoomId = false;

}
