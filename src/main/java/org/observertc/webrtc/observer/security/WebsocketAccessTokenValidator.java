package org.observertc.webrtc.observer.security;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.micronaut.security.token.jwt.generator.claims.JwtClaims;
import io.micronaut.security.token.validator.TokenValidator;
import io.micronaut.websocket.WebSocketSession;
import io.reactivex.Flowable;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class WebsocketAccessTokenValidator {
    private static final Logger logger = LoggerFactory.getLogger(WebsocketAccessTokenValidator.class);
    private static final String ACCESS_TOKEN_HOLDER_PARAMETER_NAME = "accessToken";

    private final Collection<TokenValidator> tokenValidators;
    private final boolean enabled;
    private ObserverConfig.SecurityConfig.WebsocketSecurityConfig config;

    public WebsocketAccessTokenValidator(
            Collection<TokenValidator> tokenValidators,
            ObserverConfig observerConfig
    ) {
        this.config = observerConfig.security.websockets;
        this.tokenValidators = tokenValidators;
        this.enabled = Objects.nonNull(this.tokenValidators) && 0 < this.tokenValidators.size();
    }

    public static String getAccessToken(WebSocketSession session) {
        var requestParameters = session.getRequestParameters();
        String result = requestParameters.get(ACCESS_TOKEN_HOLDER_PARAMETER_NAME);
        return result;
    }

    @PostConstruct
    void setup() {

    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isValid(String accessToken, AtomicReference<Instant> expirationHolder) {
        if (!this.enabled) {
            return true;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("validating access token {}", accessToken);
        }
        if (Objects.isNull(accessToken)) {
            if (logger.isDebugEnabled()) {
                logger.debug("access token {} is null", accessToken);
            }
            return false;
        }
        AtomicBoolean isValid = new AtomicBoolean(false);
        try {
            Flowable.fromIterable(this.tokenValidators)
                    .flatMap(tokenValidator -> {
                                return tokenValidator.validateToken(accessToken, null);
                            }
                    )
                    .subscribe(auth -> {
                        var attributes = auth.getAttributes();
                        Date expirationDate = (Date) attributes.get(JwtClaims.EXPIRATION_TIME);
                        if (logger.isDebugEnabled()) {
                            logger.debug("access token {} is valid, expiration time is", accessToken, expirationDate.toString());
                        }
                        // the token is valid
                        if (Objects.isNull(expirationDate)) {
                            if (this.config.expirationInMin == 0) {
                                logger.warn("There is no expiration set in the token and there is no explicit expiration set in the configuration. The token must expires sometime, this token {} is invalid", accessToken);
                                return;
                            }
                        }

                        Instant expires;
                        if (0 < this.config.expirationInMin) {
                            expires = Instant.now().plusSeconds(this.config.expirationInMin * 60);
                        } else {
                            expires = expirationDate.toInstant();
                        }
                        isValid.set(true);
                        if (Objects.nonNull(expirationHolder)) {
                            expirationHolder.set(expires);
                        }
                        return;
                    });
        } catch (Exception ex) {
            logger.warn("Error occurred during validating access token {}", accessToken, ex);
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("access token {} is valid", accessToken);
        }
        boolean result = isValid.get();
        if (!result && logger.isDebugEnabled()) {
            this.tryGetClosingReasonForInvalidTokens(accessToken);
        }
        return result;
    }

    private void tryGetClosingReasonForInvalidTokens(String accessToken) {
        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(accessToken);
        } catch (ParseException e) {
            logger.info("The token {} is not a JWT Signed token", accessToken, e);
            return;
        } catch (Throwable t) {
            logger.warn("Exception during parsing", t);
            return;
        }
        JWTClaimsSet claims;
        try {
            claims = signedJWT.getJWTClaimsSet();
            logger.info("Token claims are: {} ", claims);

        } catch (ParseException e) {
            logger.warn("Exception during parsing", e);
            return;
        }
        Date expiration = claims.getExpirationTime();
        if (Objects.isNull(expiration)) {
            logger.info("expiration for token is null");
            return;
        }
        Instant now = Instant.now();
        if (expiration.toInstant().compareTo(now) < 0) {
            logger.info("token is expired");
            return;
        }
        logger.debug("access token {} validation failed, no validatedAccessToken object is created", accessToken);
    }
}