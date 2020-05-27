package com.observertc.gatekeeper.webrtcstat.repositories;

import java.util.function.Supplier;
import org.jooq.DSLContext;

@FunctionalInterface
public interface IDSLContextProvider extends Supplier<DSLContext> {

}
