package org.observertc.webrtc.observer.security;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.security.token.validator.TokenValidator;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.WebSocketSession;
import io.reactivex.Flowable;
import org.observertc.webrtc.observer.common.TimeLimitedMap;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.configs.ObserverConfigDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Singleton //indicates that only one instance of annotated class will exist in the application runtime
public class WebsocketSessionValidatorInterceptor implements MethodInterceptor<Object, Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebsocketSessionValidatorInterceptor.class);
    private static final String ACCESS_TOKEN_HOLDER_PARAMETER_NAME = "accessToken";

    private TimeLimitedMap<String, CloseReason> invalidAccessTokens;
    private final Collection<TokenValidator> tokenValidators;
    private final boolean authenticate;

    private Map<String, ValidatedSession> validatedSessions;
    private ObserverConfig.SecurityConfig.WebsocketsSecurityConfig config;

    public WebsocketSessionValidatorInterceptor(Collection<TokenValidator> tokenValidators) {
        this.tokenValidators = tokenValidators;
        this.authenticate = Objects.nonNull(this.tokenValidators) && 0 < this.tokenValidators.size();
        this.validatedSessions = new ConcurrentHashMap<>();
    }

    @Inject
    ObserverConfigDispatcher configDispatcher;

    @Inject
    WebsocketSecurityCustomCloseReasons closeReasons;

    @PostConstruct
    void setup() {
        var observerConfig = configDispatcher.getConfig();
        var config = observerConfig.security.websockets;
        this.config = config;

        var invalidCacheExpiration = Duration.ofSeconds(config.invalidAccessTokensCacheExpirationInS);
        this.invalidAccessTokens = new TimeLimitedMap<>(invalidCacheExpiration);

        var revalidationPeriod = Duration.ofSeconds(config.accessTokensRevalidationPeriodInS);
        this.validatedSessions = new TimeLimitedMap(revalidationPeriod);

    }

    @Scheduled(initialDelay = "1m", fixedDelay = "1m")
    void revalidate() {
        final Instant now = Instant.now();
        final int thresholdInS = this.config.accessTokensRevalidationPeriodInS;
        Collection<String> keys = this.validatedSessions.keySet();
        for (String accessToken : keys) {
            ValidatedSession validatedSession = this.validatedSessions.get(accessToken);
            if (Duration.between(validatedSession.validated, now).getSeconds() < thresholdInS) {
                continue;
            }
            WebSocketSession session = validatedSession.session;
            boolean accessTokenIsValid;
            try {
                accessTokenIsValid = this.isValid(accessToken);
            } catch (Exception ex) {
                var closeReason = closeReasons.getValidationServerError(ex.getMessage());
                session.close(closeReason);
                this.validatedSessions.remove(accessToken);
                continue;
            }

            if (accessTokenIsValid) {
                validatedSession.prolong();
                continue;
            }

            CloseReason closeReason = closeReasons.getAccessTokenExpired();
            session.close(closeReason);
            this.invalidAccessTokens.put(accessToken, closeReason);
            this.validatedSessions.remove(accessToken);
        }
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (!this.authenticate) {
            return context.proceed();
        }
        // validate method
        var returnType = context.getReturnType();
        if (!returnType.isVoid()) {
            String message = String.format("%s method must have a void return type to apply %s",
                    context.getMethodName(), ValidateWebsocketSession.class.getSimpleName());
            throw new RuntimeException(message);
        }
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
        String accessToken = getAccessToken(session);
        if (this.hadReasonToClose(session, accessToken)) {
            return null;
        }
        this.validatedSessions.put(accessToken, new ValidatedSession(session));
        Object result = context.proceed();
        return result;
    }

    private boolean hadReasonToClose(WebSocketSession session, String accessToken) {
        CloseReason closeReason;
        if (Objects.isNull(accessToken)) {
            closeReason = closeReasons.getNoAccessTokenProvided();
            session.close(closeReason);
            return true;
        }
        closeReason = this.invalidAccessTokens.get(accessToken);
        if (Objects.nonNull(closeReason)) {
            session.close(closeReason);
            return true;
        }
        boolean accessTokenIsValid;
        try {
            accessTokenIsValid = this.isValid(accessToken);
        } catch (Exception ex) {
            closeReason = closeReasons.getValidationServerError(ex.getMessage());
            session.close(closeReason);
            return false;
        }

        if (!accessTokenIsValid) {
            closeReason = closeReasons.getInvalidAccessToken();
            this.invalidAccessTokens.put(accessToken, closeReason);
            session.close(closeReason);
            return true;
        }
        return false;
    }

    private boolean isValid(String accessToken) throws Exception {
        var authenticated = Flowable.fromIterable(this.tokenValidators)
                .flatMap(tokenValidator -> tokenValidator.validateToken(accessToken, null));
        boolean result = 0 < authenticated.count().blockingGet();
        return result;
    }

    private static String getAccessToken(WebSocketSession session) {
        var requestParameters = session.getRequestParameters();
        String result = requestParameters.get(ACCESS_TOKEN_HOLDER_PARAMETER_NAME);
        return result;
    }

    private class ValidatedSession {
        final WebSocketSession session;
        Instant validated;

        ValidatedSession(WebSocketSession session) {
            this.session = session;
            this.validated = Instant.now();
        }

        void prolong() {
            this.validated = Instant.now();
        }
    }
}