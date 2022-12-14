package org.observertc.observer.hamokendpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class EndpointInternalMessage {

    private static final Logger logger = LoggerFactory.getLogger(EndpointInternalMessage.class);

    public enum MessageType {
        CLIENT_HELLO,
        SERVER_HELLO,

    }

    public UUID requestId = null;
    public MessageType type = null;
    public UUID remoteEndpointId;
    public UUID connectionId = null;
    public String remoteHost = null;
    public Integer remotePort = null;


    public record ClientHello(
            UUID connectionId,
            String remoteHost,
            int remotePort,
            UUID remoteEndpointId
    ) {

    }

    public record ServerHello(
            String remoteHost,
            int remotePort,
            UUID remoteEndpointId
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


    public ClientHello asClientHello() {
        if (!MessageType.CLIENT_HELLO.equals(this.type)) {
            logger.warn("Cannot make a {} from {}", MessageType.CLIENT_HELLO, this.type);
            return null;
        }
        return new ClientHello(
                this.connectionId,
                this.remoteHost,
                this.remotePort,
                this.remoteEndpointId
        );
    }

    public ServerHello asServerHello() {
        if (!MessageType.SERVER_HELLO.equals(this.type)) {
            logger.warn("Cannot make a {} from {}", MessageType.SERVER_HELLO, this.type);
            return null;
        }
        return new ServerHello(
                this.remoteHost,
                this.remotePort,
                this.remoteEndpointId
        );
    }


    public static EndpointInternalMessage createClientHello(UUID connectionId, UUID endpointId, String host, int port) {
        var result = new EndpointInternalMessage();
        result.type = MessageType.CLIENT_HELLO;
        result.connectionId = connectionId;
        result.remoteHost = host;
        result.remoteEndpointId = endpointId;
        result.remotePort = port;
        return result;
    }

    public static EndpointInternalMessage createServerHello(UUID endpointId, String host, int port) {
        var result = new EndpointInternalMessage();
        result.type = MessageType.SERVER_HELLO;
        result.remoteHost = host;
        result.remoteEndpointId = endpointId;
        result.remotePort = port;
        return result;
    }
}
