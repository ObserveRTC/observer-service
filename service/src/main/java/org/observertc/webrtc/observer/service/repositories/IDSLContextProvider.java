package org.observertc.webrtc.observer.service.repositories;

import java.util.function.Supplier;
import org.jooq.DSLContext;

@FunctionalInterface
public interface IDSLContextProvider extends Supplier<DSLContext> {

}
