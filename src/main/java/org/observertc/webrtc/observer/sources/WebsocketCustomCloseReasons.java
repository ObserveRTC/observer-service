package org.observertc.webrtc.observer.sources;

import io.micronaut.websocket.CloseReason;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class WebsocketCustomCloseReasons {
    private static final int ACCESS_TOKEN_EXPIRED_CODE = 4224;
    private static final int NO_ACCESS_TOKEN_PROVIDED_CODE = 4225;
    private static final int INVALID_ACCESS_TOKEN_CODE = 4226;
    private static final int INTERNAL_SERVER_ERROR_CODE = 4227;
    private static final int TOO_MANY_WEBSOCKET_FOR_SAME_ACCESS_TOKENS = 4228;
    private static final int WEBSOCKET_IS_DISABLED = 4229;

    private static final int INVALID_INPUT = 4230;

    private final Map<Integer, CloseReason> reasons;

    public WebsocketCustomCloseReasons() {
        this.reasons = new HashMap<>();
    }

    @PostConstruct
    void setup() {
        this
                .set(ACCESS_TOKEN_EXPIRED_CODE, "Access token is expired")
                .set(NO_ACCESS_TOKEN_PROVIDED_CODE, "Access token is required to provide")
                .set(INVALID_ACCESS_TOKEN_CODE, "The provided access token is invalid")
                .set(TOO_MANY_WEBSOCKET_FOR_SAME_ACCESS_TOKENS, "There are too many websocket registered for the same access token")
                .set(WEBSOCKET_IS_DISABLED, "The websocket is disabled")
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

    public CloseReason getInternalServerError(String message) {
        var result = new CloseReason(INTERNAL_SERVER_ERROR_CODE, message);
        return result;
    }

    public CloseReason getInvalidInput(String message) {
        var result = new CloseReason(INVALID_INPUT, message);
        return result;
    }

    public CloseReason getTooManyWebsocketRegisteredForTheSameAccessToken() {
        var result = this.reasons.get(TOO_MANY_WEBSOCKET_FOR_SAME_ACCESS_TOKENS);
        return result;
    }

    public CloseReason getWebsocketIsDisabled() {
        var result = this.reasons.get(WEBSOCKET_IS_DISABLED);
        return result;
    }

    private WebsocketCustomCloseReasons set(int code, String text) {
        CloseReason reason = new CloseReason(code, text);
        this.reasons.put(code, reason);
        return this;
    }
}
