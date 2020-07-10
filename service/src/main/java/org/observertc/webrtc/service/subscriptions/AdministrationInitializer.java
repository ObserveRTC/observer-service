package org.observertc.webrtc.service.subscriptions;

import javax.inject.Singleton;
import org.jooq.DSLContext;
import org.observertc.webrtc.service.repositories.IDSLContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AdministrationInitializer implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(AdministrationInitializer.class);
	private final IDSLContextProvider contextProvider;
	private final Stage<DSLContext> pipeline;
	private volatile boolean run = false;

	public AdministrationInitializer(IDSLContextProvider dslContextProvider) {
		contextProvider = dslContextProvider;
		this.pipeline = new StagePipelineBuilder<DSLContext>()
				.withStage(this::makeAdminCheck)
				.withStage(this::makeObserverCheck)
				.getFirst();
	}

	@Override
	public void run() {
		if (this.run) {
			logger.warn("The administrative initializer has already run");
		}
		this.run = true;
		DSLContext context = this.contextProvider.get();
		this.pipeline.accept(context);
	}

	private Stage<DSLContext> makeAdminCheck() {
		return new Stage<DSLContext>() {
			@Override
			public void accept(DSLContext dslContext) {
				// TODO: task here
				super.accept(dslContext);
			}
		};
	}

	private Stage<DSLContext> makeObserverCheck() {
		return new Stage<DSLContext>() {
			@Override
			public void accept(DSLContext dslContext) {
				// TODO: task here
				super.accept(dslContext);
			}
		};
	}


}
