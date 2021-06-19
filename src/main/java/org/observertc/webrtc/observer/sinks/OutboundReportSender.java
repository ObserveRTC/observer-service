package org.observertc.webrtc.observer.sinks;

import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.observer.Connectors;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.dto.OutboundReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class OutboundReportSender implements Consumer<OutboundReport> {

    private static final Logger logger = LoggerFactory.getLogger(Connectors.class);

    public OutboundReportSender(ObserverConfig config) {

    }

    @Override
    public void accept(OutboundReport outboundReport) throws Throwable {

    }
}
