package org.observertc.webrtc.observer.connectors.encoders;

import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.observertc.webrtc.observer.connectors.MessageFormat;

public interface Encoder extends Function<Report, EncodedRecord> {
    Encoder withMessageFormat(MessageFormat format);
    MessageFormat getMessageFormat();
}
