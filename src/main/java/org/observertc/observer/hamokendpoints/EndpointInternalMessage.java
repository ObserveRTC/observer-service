package org.observertc.observer.hamokendpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class EndpointInternalMessage {

    private static final Logger logger = LoggerFactory.getLogger(EndpointInternalMessage.class);

    public enum MessageType {
        OPEN_NOTIFICATION,
        STATE_REQUEST,
        STATE_RESPONSE
    }

    public UUID requestId = null;
    public MessageType type = null;
    public UUID remoteEndpointId;
    public UUID connectionId = null;
    public String remoteHost = null;
    public Integer remotePort = null;


    public record StateRequest(
            UUID requestId
    ) {

    }

    public record StateResponse(
            UUID requestId,
            UUID remoteEndpointId,
            UUID connectionId,
            String remoteHost,
            int remotePort
    ) {

    }

    public record OpenNotification(
            UUID remoteEndpointId
    ) {

    }

    public EndpointInternalMessage createStateResponse(UUID localEndpointId, UUID connectionId, String host, int port) {
        if (!MessageType.STATE_REQUEST.equals(this.type)) {
            logger.warn("Cannot make a {} from {}", MessageType.STATE_RESPONSE, MessageType.STATE_REQUEST);
            return null;
        }
        var result = new EndpointInternalMessage();
        result.type = MessageType.STATE_RESPONSE;
        result.requestId = this.requestId;
        result.remoteEndpointId = localEndpointId;
        result.connectionId = connectionId;
        result.remoteHost = host;
        result.remotePort = port;
        return result;
    }

    public StateRequest asStateRequest() {
        if (!MessageType.STATE_REQUEST.equals(this.type)) {
            logger.warn("Cannot make a {} from {}", MessageType.STATE_REQUEST, this.type);
            return null;
        }
        return new StateRequest(
                this.requestId
        );
    }

    public StateResponse asStateResponse() {
        if (!MessageType.STATE_RESPONSE.equals(this.type)) {
            logger.warn("Cannot make a {} from {}", MessageType.STATE_RESPONSE, this.type);
            return null;
        }
        return new StateResponse(
                this.requestId,
                this.remoteEndpointId,
                this.connectionId,
                this.remoteHost,
                this.remotePort
        );
    }

    public OpenNotification asOpenNotification() {
        if (!MessageType.OPEN_NOTIFICATION.equals(this.type)) {
            logger.warn("Cannot make a {} from {}", MessageType.OPEN_NOTIFICATION, this.type);
            return null;
        }
        return new OpenNotification(
                this.remoteEndpointId
        );
    }

    public static EndpointInternalMessage createStateRequest(UUID requestId) {
        var result = new EndpointInternalMessage();
        result.requestId = requestId;
        result.type = MessageType.STATE_REQUEST;
        return result;
    }

    public static EndpointInternalMessage createOpenNotification(UUID remoteEndpointId) {
        var result = new EndpointInternalMessage();
        result.type = MessageType.OPEN_NOTIFICATION;
        result.remoteEndpointId = remoteEndpointId;
        return result;
    }
}
