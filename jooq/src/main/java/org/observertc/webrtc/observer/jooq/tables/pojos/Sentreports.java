/*
 * This file is generated by jOOQ.
 */
package org.observertc.webrtc.observer.jooq.tables.pojos;


import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * SentReports
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Sentreports implements Serializable {

    private static final long serialVersionUID = -1776259350;

    private final byte[]        signature;
    private final byte[]        peerconnectionuuid;
    private final LocalDateTime reported;

    public Sentreports(Sentreports value) {
        this.signature = value.signature;
        this.peerconnectionuuid = value.peerconnectionuuid;
        this.reported = value.reported;
    }

    public Sentreports(
        byte[]        signature,
        byte[]        peerconnectionuuid,
        LocalDateTime reported
    ) {
        this.signature = signature;
        this.peerconnectionuuid = peerconnectionuuid;
        this.reported = reported;
    }

    @NotNull
    @Size(max = 255)
    public byte[] getSignature() {
        return this.signature;
    }

    @Size(max = 16)
    public byte[] getPeerconnectionuuid() {
        return this.peerconnectionuuid;
    }

    public LocalDateTime getReported() {
        return this.reported;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Sentreports (");

        sb.append("[binary...]");
        sb.append(", ").append("[binary...]");
        sb.append(", ").append(reported);

        sb.append(")");
        return sb.toString();
    }
}