package org.observertc.webrtc.service.initializers;

import java.util.function.Consumer;

abstract class Stage<T> implements Consumer<T> {
	Stage next;

	@Override
	public void accept(T dslContext) {
		if (this.next != null) {
			this.andThen(this.next);
		}
	}
}
