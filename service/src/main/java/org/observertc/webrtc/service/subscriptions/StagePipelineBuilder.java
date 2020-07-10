package org.observertc.webrtc.service.subscriptions;

import java.util.function.Supplier;

class StagePipelineBuilder<T> {
	private Stage<T> first;
	private Stage<T> actual;

	public Stage<T> getFirst() {
		return this.first;
	}

	public StagePipelineBuilder<T> withStage(Supplier<Stage<T>> stageSupplier) {
		return this.withStage(stageSupplier.get());
	}

	public StagePipelineBuilder<T> withStage(Stage<T> stage) {
		if (this.first == null) {
			this.first = stage;
			this.actual = stage;
			return this;
		}
		this.actual.next = stage;
		this.actual = stage;
		return this;
	}
}
