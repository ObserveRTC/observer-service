package org.observertc.webrtc.observer.security;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.websocket.WebSocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WebsocketSessionInvalidatorInterceptor implements MethodInterceptor<Object, Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebsocketSessionInvalidatorInterceptor.class);
    private static final String ACCESS_TOKEN_HOLDER_PARAMETER_NAME = "accessToken";

    public WebsocketSessionInvalidatorInterceptor() {

    }

    @Inject
    WebsocketSessionValidator sessionValidator;

    @PostConstruct
    void setup() {

    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (!sessionValidator.isEnabled()) {
            return context.proceed();
        }
        WebSocketSession session = WebsocketSessionValidator.extractWebsocketSession(context, InvalidateWebsocketSession.class);
        if (sessionValidator.isEnabled()) {
            sessionValidator.makeInvalid(session);
        }
        Object result = context.proceed();
        return result;
    }


}