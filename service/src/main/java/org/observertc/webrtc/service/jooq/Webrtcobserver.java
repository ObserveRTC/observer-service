/*
 * This file is generated by jOOQ.
 */
package org.observertc.webrtc.service.jooq;


import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;
import org.observertc.webrtc.service.jooq.tables.Callpeerconnections;
import org.observertc.webrtc.service.jooq.tables.Observerorganisations;
import org.observertc.webrtc.service.jooq.tables.Observers;
import org.observertc.webrtc.service.jooq.tables.Organisations;
import org.observertc.webrtc.service.jooq.tables.Peerconnectionssrcs;
import org.observertc.webrtc.service.jooq.tables.Sentreports;
import org.observertc.webrtc.service.jooq.tables.Users;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Webrtcobserver extends SchemaImpl {

    private static final long serialVersionUID = -754160247;

    /**
     * The reference instance of <code>WebRTCObserver</code>
     */
    public static final Webrtcobserver WEBRTCOBSERVER = new Webrtcobserver();

    /**
     * CallIDs
     */
    public final Callpeerconnections CALLPEERCONNECTIONS = Callpeerconnections.CALLPEERCONNECTIONS;

    /**
     * An associative table to map Observers to Evaluators
     */
    public final Observerorganisations OBSERVERORGANISATIONS = Observerorganisations.OBSERVERORGANISATIONS;

    /**
     * Observers
     */
    public final Observers OBSERVERS = Observers.OBSERVERS;

    /**
     * Organizations
     */
    public final Organisations ORGANISATIONS = Organisations.ORGANISATIONS;

    /**
     * A table to map peer connections to SSRCs
     */
    public final Peerconnectionssrcs PEERCONNECTIONSSRCS = Peerconnectionssrcs.PEERCONNECTIONSSRCS;

    /**
     * SentReports
     */
    public final Sentreports SENTREPORTS = Sentreports.SENTREPORTS;

    /**
     * Users
     */
    public final Users USERS = Users.USERS;

    /**
     * No further instances allowed
     */
    private Webrtcobserver() {
        super("WebRTCObserver", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.<Table<?>>asList(
            Callpeerconnections.CALLPEERCONNECTIONS,
            Observerorganisations.OBSERVERORGANISATIONS,
            Observers.OBSERVERS,
            Organisations.ORGANISATIONS,
            Peerconnectionssrcs.PEERCONNECTIONSSRCS,
            Sentreports.SENTREPORTS,
            Users.USERS);
    }
}
