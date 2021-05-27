package org.observertc.webrtc.observer.security;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.security.token.jwt.generator.claims.JwtClaims;
import io.micronaut.security.token.validator.TokenValidator;
import io.micronaut.websocket.WebSocketSession;
import io.reactivex.Flowable;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.configs.ObserverConfigDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class WebsocketSessionValidator {
    private static final Logger logger = LoggerFactory.getLogger(WebsocketSessionValidator.class);
    private static final String ACCESS_TOKEN_HOLDER_PARAMETER_NAME = "accessToken";

    static WebSocketSession extractWebsocketSession(MethodInvocationContext<Object, Object> context, Class annotationClass) {
        var returnType = context.getReturnType();
        if (!returnType.isVoid()) {
            String message = String.format("%s method must have a void return type to apply %s",
                    context.getMethodName(), annotationClass.getSimpleName());
            throw new RuntimeException(message);
        }

        var arguments = context.getArguments();
        var websocketParameterNameHolder = Arrays.stream(arguments)
                .filter(argument -> argument.getType().isAssignableFrom(WebSocketSession.class))
                .map(argument -> argument.getName())
                .findFirst();
        if (websocketParameterNameHolder.isEmpty()) {
            String message = String.format("%s method must contain %s instance in its parameters to run %s interceptor",
                    context.getMethodName(), WebSocketSession.class.getSimpleName(), annotationClass.getSimpleName());
            throw new RuntimeException(message);
        }
        String parameterName = websocketParameterNameHolder.get();
        WebSocketSession result = (WebSocketSession) context.getParameterValueMap().get(parameterName);
        return result;
    }

    private final Collection<TokenValidator> tokenValidators;
    private final boolean enabled;
    private ConcurrentHashMap<String, ValidatedAccessToken> validatedAccessTokens;
    private BlockingQueue<InvalidatedSessionId> invalidatedSessionIds;
    private ObserverConfig.SecurityConfig.WebsocketSecurityConfig config;

    public WebsocketSessionValidator(
            Collection<TokenValidator> tokenValidators,
            ObserverConfigDispatcher configDispatcher
    ) {
        this.config = configDispatcher.getConfig().security.websockets;
        this.tokenValidators = tokenValidators;
        this.enabled = Objects.nonNull(this.tokenValidators) && 0 < this.tokenValidators.size();
        this.validatedAccessTokens = new ConcurrentHashMap<>();
        this.invalidatedSessionIds = new LinkedBlockingQueue<>();
    }

    @Inject
    WebsocketSecurityCustomCloseReasons closeReasons;

    @PostConstruct
    void setup() {

    }

    @Scheduled(initialDelay = "1m", fixedDelay = "1m")
    void invalidateSessions() {
        Instant now = Instant.now();
        Set<String> accessTokens = this.validatedAccessTokens.keySet();
        for (String accessToken : accessTokens) {
            ValidatedAccessToken validatedAccessToken = this.validatedAccessTokens.get(accessToken);
            if (0 < validatedAccessToken.expires.compareTo(now)) {
                continue;
            }
            Collection<WebSocketSession> sessions = validatedAccessToken.sessions.values();
            this.validatedAccessTokens.remove(validatedAccessToken.accessToken);
            var closeReason = closeReasons.getAccessTokenExpired();
            sessions.stream().forEach(session -> {
                try {
                    if (!session.isOpen()) {
                        return;
                    }
                    session.close(closeReason);
                } catch (Exception ex) {
                    logger.warn("Exception occurred while closing session", ex);
                }
            });
        }

        while (!this.invalidatedSessionIds.isEmpty()) {
            InvalidatedSessionId invalidatedSessionId = this.invalidatedSessionIds.poll();
            ValidatedAccessToken validatedAccessToken = this.validatedAccessTokens.get(invalidatedSessionId.accessToken);
            if (Objects.isNull(validatedAccessToken)) {
                continue;
            }
            WebSocketSession session = validatedAccessToken.sessions.remove(invalidatedSessionId.sessionId);
            if (Objects.isNull(session)) {
                continue;
            }
            if (session.isOpen()) {
                var closeReason = closeReasons.getInvalidAccessToken();
                session.close(closeReason);
            }
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void makeInvalid(WebSocketSession session) {
        String accessToken = getAccessToken(session);
        if (Objects.isNull(accessToken)) {
            return;
        }
        InvalidatedSessionId invalidatedSessionId = new InvalidatedSessionId(accessToken, session.getId());
        this.invalidatedSessionIds.add(invalidatedSessionId);
    }

    public boolean isValid(WebSocketSession session) {
        String accessToken = getAccessToken(session);
        if (!this.enabled) {
            return true;
        }

        if (Objects.isNull(accessToken)) {
            var closeReason = closeReasons.getNoAccessTokenProvided();
            session.close(closeReason);
            return false;
        }
        ValidatedAccessToken validatedAccessToken = this.validatedAccessTokens.get(accessToken);
        if (Objects.nonNull(validatedAccessToken)) {
            if (0 < this.config.maxValidatedSessionsForOneAccessToken) {
                if (this.config.maxValidatedSessionsForOneAccessToken <= validatedAccessToken.sessions.size()) {
                    var closeReason = closeReasons.getTooManyWebsocketRegisteredForTheSameAccessToken();
                    session.close(closeReason);
                    return false;
                }
            }
            validatedAccessToken.addSession(session);
            return true;
        }
        AtomicReference<ValidatedAccessToken> validatedAccessTokenRef =  new AtomicReference<>(null);
        try {
            Flowable.fromIterable(this.tokenValidators)
                    .flatMap(tokenValidator -> tokenValidator.validateToken(accessToken, null))
                    .subscribe(auth -> {
                        var attributes = auth.getAttributes();
                        Date expirationDate = (Date) attributes.get(JwtClaims.EXPIRATION_TIME);
                        if (Objects.isNull(expirationDate)) {
                            return;
                        }
                        Instant expires = expirationDate.toInstant();
                        ValidatedAccessToken newValidatedAccessToken = new ValidatedAccessToken(accessToken, expires).addSession(session);
                        validatedAccessTokenRef.set(newValidatedAccessToken);
                        return;
                    });
        } catch (Exception ex) {
            var closeReason = closeReasons.getInternalServerError(ex.getMessage());
            session.close(closeReason);
            return false;
        }

        validatedAccessToken = validatedAccessTokenRef.get();
        if (Objects.isNull(validatedAccessToken)) {
            var closeReason = closeReasons.getInvalidAccessToken();
            session.close(closeReason);
            return false;
        }
        this.validatedAccessTokens.put(accessToken, validatedAccessToken);
        return true;
    }

    private static String getAccessToken(WebSocketSession session) {
        var requestParameters = session.getRequestParameters();
        String result = requestParameters.get(ACCESS_TOKEN_HOLDER_PARAMETER_NAME);
        return result;
    }

    private class ValidatedAccessToken implements Comparable<ValidatedAccessToken>{
        final String accessToken;
        final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
        final Instant expires;

        ValidatedAccessToken(String accessToken, Instant expires) {
            this.accessToken = accessToken;
            this.expires = expires;
        }

        ValidatedAccessToken addSession(WebSocketSession session) {
            this.sessions.put(session.getId(), session);
            return this;
        }

        @Override
        public int compareTo(ValidatedAccessToken o) {
            return this.expires.compareTo(o.expires);
        }
    }

    private class InvalidatedSessionId {
        final String accessToken;
        final String sessionId;

        private InvalidatedSessionId(String accessToken, String sessionId) {
            this.accessToken = accessToken;
            this.sessionId = sessionId;
        }
    }
}