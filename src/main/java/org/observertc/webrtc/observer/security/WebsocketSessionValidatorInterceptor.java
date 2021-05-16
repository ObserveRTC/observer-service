package org.observertc.webrtc.observer.security;

import io.micronaut.aop.MethodInterceptor;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

@Singleton //indicates that only one instance of annotated class will exist in the application runtime
public class WebsocketSessionValidatorInterceptor implements MethodInterceptor<Object, Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebsocketSessionValidatorInterceptor.class);
    private static final String ACCESS_TOKEN_HOLDER_PARAMETER_NAME = "accessToken";

    private final Collection<TokenValidator> tokenValidators;
    private final boolean doValidate;
    private ConcurrentHashMap<String, ValidatedAccessToken> validatedAccessTokens;
    private PriorityBlockingQueue<ValidatedAccessToken> expiringSessions;
    private ObserverConfig.SecurityConfig.WebsocketSecurityConfig config;

    public WebsocketSessionValidatorInterceptor(
            Collection<TokenValidator> tokenValidators,
            ObserverConfigDispatcher configDispatcher
    ) {
        this.config = configDispatcher.getConfig().security.websockets;
        this.tokenValidators = tokenValidators;
        this.doValidate = Objects.nonNull(this.tokenValidators) && 0 < this.tokenValidators.size();
        this.validatedAccessTokens = new ConcurrentHashMap<>();
        this.expiringSessions = new PriorityBlockingQueue<>();
    }

    @Inject
    WebsocketSecurityCustomCloseReasons closeReasons;

    @PostConstruct
    void setup() {

    }

    @Scheduled(initialDelay = "1m", fixedDelay = "1m")
    void invalidateSessions() {
        Instant now = Instant.now();
        while (!this.expiringSessions.isEmpty()) {
            ValidatedAccessToken validatedAccessToken = this.expiringSessions.peek();
            if (0 < validatedAccessToken.expires.compareTo(now)) {
                break;
            }
            this.expiringSessions.poll();
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
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (!this.doValidate) {
            return context.proceed();
        }
        // validate method
        var returnType = context.getReturnType();
        if (!returnType.isVoid()) {
            String message = String.format("%s method must have a void return type to apply %s",
                    context.getMethodName(), ValidateWebsocketSession.class.getSimpleName());
            throw new RuntimeException(message);
        }

        var annotationValue = context.getAnnotation(ValidateWebsocketSession.class);
        var arguments = context.getArguments();
        var websocketParameterNameHolder = Arrays.stream(arguments)
                .filter(argument -> argument.getType().isAssignableFrom(WebSocketSession.class))
                .map(argument -> argument.getName())
                .findFirst();
        if (websocketParameterNameHolder.isEmpty()) {
            String message = String.format("%s method must contain %s instance in its parameters to run %s interceptor",
                    context.getMethodName(), WebSocketSession.class.getSimpleName(), ValidateWebsocketSession.class.getSimpleName());
            throw new RuntimeException(message);
        }
        String parameterName = websocketParameterNameHolder.get();
        WebSocketSession session = (WebSocketSession) context.getParameterValueMap().get(parameterName);
        if (this.doValidate) {
            boolean valid = false;
            try {
                valid = this.isValid(session);
            } catch (Exception ex) {
                if (session.isOpen()) {
                    var closeReason = closeReasons.getInternalServerError(ex.getMessage());
                    session.close(closeReason);
                }
                return null;
            }
            if (!valid) {
                return null;
            }
        }
        Object result = context.proceed();
        return result;
    }

    private boolean isValid(WebSocketSession session) {
        String accessToken = getAccessToken(session);
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
        this.expiringSessions.add(validatedAccessToken);
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
}