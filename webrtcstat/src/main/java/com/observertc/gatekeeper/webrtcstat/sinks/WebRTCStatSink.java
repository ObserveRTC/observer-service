package com.observertc.gatekeeper.webrtcstat.sinks;

import com.observertc.gatekeeper.dto.WebRTCStatDTO;
import java.util.function.Consumer;

public interface WebRTCStatSink extends Consumer<WebRTCStatDTO> {

}
