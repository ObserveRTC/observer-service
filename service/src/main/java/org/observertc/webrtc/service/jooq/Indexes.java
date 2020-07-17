/*
 * This file is generated by jOOQ.
 */
package org.observertc.webrtc.service.jooq;


import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;
import org.observertc.webrtc.service.jooq.tables.Observerorganisations;
import org.observertc.webrtc.service.jooq.tables.Peerconnectionssrcs;
import org.observertc.webrtc.service.jooq.tables.Sentreports;
import org.observertc.webrtc.service.jooq.tables.Users;


/**
 * A class modelling indexes of tables of the <code>WebRTCObserver</code> 
 * schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index OBSERVERORGANISATIONS_OBSERVER_ID = Indexes0.OBSERVERORGANISATIONS_OBSERVER_ID;
    public static final Index OBSERVERORGANISATIONS_ORGANISATION_ID = Indexes0.OBSERVERORGANISATIONS_ORGANISATION_ID;
    public static final Index PEERCONNECTIONSSRCS_PEERCONNECTIONSSRCS_OBSERVER_SSRC_INDEX = Indexes0.PEERCONNECTIONSSRCS_PEERCONNECTIONSSRCS_OBSERVER_SSRC_INDEX;
    public static final Index PEERCONNECTIONSSRCS_PEERCONNECTIONSSRCS_PEERCONNECTION_INDEX = Indexes0.PEERCONNECTIONSSRCS_PEERCONNECTIONSSRCS_PEERCONNECTION_INDEX;
    public static final Index PEERCONNECTIONSSRCS_PEERCONNECTIONSSRCS_UPDATED_INDEX = Indexes0.PEERCONNECTIONSSRCS_PEERCONNECTIONSSRCS_UPDATED_INDEX;
    public static final Index SENTREPORTS_SENT_REPORTS_TYPE_INDEX = Indexes0.SENTREPORTS_SENT_REPORTS_TYPE_INDEX;
    public static final Index USERS_USERS_USERNAME_KEY = Indexes0.USERS_USERS_USERNAME_KEY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 {
        public static Index OBSERVERORGANISATIONS_OBSERVER_ID = Internal.createIndex("observer_id", Observerorganisations.OBSERVERORGANISATIONS, new OrderField[] { Observerorganisations.OBSERVERORGANISATIONS.OBSERVER_ID }, false);
        public static Index OBSERVERORGANISATIONS_ORGANISATION_ID = Internal.createIndex("organisation_id", Observerorganisations.OBSERVERORGANISATIONS, new OrderField[] { Observerorganisations.OBSERVERORGANISATIONS.ORGANISATION_ID }, false);
        public static Index PEERCONNECTIONSSRCS_PEERCONNECTIONSSRCS_OBSERVER_SSRC_INDEX = Internal.createIndex("PeerconnectionSSRCs_observer_ssrc_index", Peerconnectionssrcs.PEERCONNECTIONSSRCS, new OrderField[] { Peerconnectionssrcs.PEERCONNECTIONSSRCS.OBSERVERUUID, Peerconnectionssrcs.PEERCONNECTIONSSRCS.SSRC }, false);
        public static Index PEERCONNECTIONSSRCS_PEERCONNECTIONSSRCS_PEERCONNECTION_INDEX = Internal.createIndex("PeerconnectionSSRCs_peerConnection_index", Peerconnectionssrcs.PEERCONNECTIONSSRCS, new OrderField[] { Peerconnectionssrcs.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID }, false);
        public static Index PEERCONNECTIONSSRCS_PEERCONNECTIONSSRCS_UPDATED_INDEX = Internal.createIndex("PeerconnectionSSRCs_updated_index", Peerconnectionssrcs.PEERCONNECTIONSSRCS, new OrderField[] { Peerconnectionssrcs.PEERCONNECTIONSSRCS.UPDATED }, false);
        public static Index SENTREPORTS_SENT_REPORTS_TYPE_INDEX = Internal.createIndex("sent_reports_type_index", Sentreports.SENTREPORTS, new OrderField[] { Sentreports.SENTREPORTS.TYPE }, false);
        public static Index USERS_USERS_USERNAME_KEY = Internal.createIndex("users_username_key", Users.USERS, new OrderField[] { Users.USERS.USERNAME }, false);
    }
}
