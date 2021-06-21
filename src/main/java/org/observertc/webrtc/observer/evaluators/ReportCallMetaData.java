package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.observer.samples.CallSamples;
import org.observertc.webrtc.observer.samples.ClientSample;
import org.observertc.webrtc.observer.samples.ClientSamples;

public class ReportCallMetaData implements Consumer<CallSamples> {

    @Override
    public void accept(CallSamples callSamples) throws Throwable {
        for (ClientSamples clientSamples : callSamples) {
            for (ClientSample clientSample : clientSamples) {
                
            }
        }
    }
}
