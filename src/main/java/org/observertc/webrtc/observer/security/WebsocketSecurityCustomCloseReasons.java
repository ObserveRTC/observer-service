package org.observertc.webrtc.observer.security;

import io.micronaut.websocket.CloseReason;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class WebsocketSecurityCustomCloseReasons {
    private static final int ACCESS_TOKEN_EXPIRED_CODE = 4224;
    private static final int NO_ACCESS_TOKEN_PROVIDED_CODE = 4225;
    private static final int INVALID_ACCESS_TOKEN_CODE = 4226;

    private final Map<Integer, CloseReason> reasons;

    public WebsocketSecurityCustomCloseReasons() {
        this.reasons = new HashMap<>();
    }

    @PostConstruct
    void setup() {
        this
                .set(ACCESS_TOKEN_EXPIRED_CODE, "Access token is expired")
                .set(NO_ACCESS_TOKEN_PROVIDED_CODE, "Access token is required to provide")
                .set(INVALID_ACCESS_TOKEN_CODE, "The provided access token is invalid")
                ;
    }

    public CloseReason getInvalidAccessToken() {
        var result = this.reasons.get(INVALID_ACCESS_TOKEN_CODE);
        return result;
    }

    public CloseReason getNoAccessTokenProvided() {
        var result = this.reasons.get(NO_ACCESS_TOKEN_PROVIDED_CODE);
        return result;
    }

    public CloseReason getAccessTokenExpired() {
        var result = this.reasons.get(ACCESS_TOKEN_EXPIRED_CODE);
        return result;
    }

    private WebsocketSecurityCustomCloseReasons set(int code, String text) {
        CloseReason reason = new CloseReason(code, text);
        this.reasons.put(code, reason);
        return this;
    }
}
