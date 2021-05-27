package org.observertc.webrtc.observer.security;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.websocket.WebSocketSession;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WebsocketSessionValidatorInterceptor implements MethodInterceptor<Object, Object> {
    public WebsocketSessionValidatorInterceptor() {

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
        WebSocketSession session = WebsocketSessionValidator.extractWebsocketSession(context, ValidateWebsocketSession.class);
        if (sessionValidator.isEnabled()) {
            boolean valid = sessionValidator.isValid(session);
            if (!valid) {
                return null;
            }
        }
        Object result = context.proceed();
        return result;
    }
}