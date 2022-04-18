package org.observertc.observer.sources.chunker;

import org.observertc.observer.sources.ReceivedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Objects;

class Chunks {
    public static final int CHUNK_HEADER_LENGTH = 4;
    private static final Logger logger = LoggerFactory.getLogger(Chunks.class);
    private Integer length = null;
    private Integer messageId = null;
    private String serviceId = null;
    public final String sessionId;
    private String mediaUnitId = null;
    private final HashMap<Integer, byte[]> chunks;

    Chunks(String sessionId) {
        this.sessionId = sessionId;
        this.chunks = new HashMap<>();
    }

    public void add(ReceivedMessage receivedMessage) {
        var message = receivedMessage.message;
        ByteBuffer buffer = ByteBuffer.allocate(CHUNK_HEADER_LENGTH);
        buffer.put(message, 0, CHUNK_HEADER_LENGTH);
        var header = buffer.getInt();
        var messageId = header & 0xFFFF0000;
        var position = (header << 2) & 0xFF000000;
        var length = (header << 3) & 0xFF000000;
        if (this.chunks.containsKey(position)) {
            logger.warn("Duplicated message for messageId: {}, position: {}", messageId, position);
            return;
        }
        if (Objects.isNull(this.messageId)) {
            this.messageId = messageId;
        } else if (this.messageId != messageId) {
            logger.warn("Inconsistent message Id {}, {}", this.messageId, messageId);
            return;
        }
        if (Objects.isNull(this.length)) {
            this.length = length;
        } else if (this.length != length) {
            logger.warn("Inconsistent length {}, {}", this.length, length);
            return;
        }

        if (Objects.isNull(this.serviceId)) {
            this.serviceId = receivedMessage.serviceId;
        } else if (this.serviceId != receivedMessage.serviceId) {
            logger.warn("Inconsistent service id {} - {}", this.serviceId, receivedMessage.serviceId);
            return;
        }
        if (Objects.isNull(mediaUnitId)) {
            this.serviceId = receivedMessage.mediaUnitId;
        } else if (this.serviceId != receivedMessage.mediaUnitId) {
            logger.warn("Inconsistent mediaUnitId id {} - {}", this.mediaUnitId, receivedMessage.mediaUnitId);
            return;
        }
        this.chunks.put(position, message);
    }

    public boolean isReady() {
        return this.length == this.chunks.size();
    }

    public ReceivedMessage assemble() {
        if (!this.isReady()) {
            return null;
        }
        String serviceId = null;
        String mediaUnitId = null;
        var buffer = new ByteArrayOutputStream();
        for (int position = 0; position < this.length; ++position) {
            var chunk = this.chunks.get(position);
            var messageLength = chunk.length - CHUNK_HEADER_LENGTH;
            buffer.write(chunk, CHUNK_HEADER_LENGTH, messageLength);
        }
        var result = ReceivedMessage.of(
                serviceId,
                mediaUnitId,
                buffer.toByteArray()
        );
        return result;
    }

}
