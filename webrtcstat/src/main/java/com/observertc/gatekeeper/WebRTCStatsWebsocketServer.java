package com.observertc.gatekeeper;

import com.observertc.gatekeeper.dto.ObserverDTO;
import com.observertc.gatekeeper.dto.WebRTCStatDTO;
import com.observertc.gatekeeper.micrometer.WebRTCStatsEvaluators;
import com.observertc.gatekeeper.repositories.ObserverRepository;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

@ServerWebSocket("/ws/{observerUUID}")
public class WebRTCStatsWebsocketServer {

	private final ObserverRepository observerRepository;
	private final Consumer<WebRTCStatDTO> webRTCStatDTOSink;
	private final WebRTCStatsEvaluators evaluators;
//	private final IDSLContextProvider contextProvider;

//	public DemoWebsocketServer(ObserverRepository observerRepository, DemoSink sink) {
//		this.observerRepository = observerRepository;
//		this.sink = sink;
//	}

	public WebRTCStatsWebsocketServer(ObserverRepository observerRepository, DemoSampleSink sampleSink, WebRTCStatsEvaluators evaluators) {
		this.observerRepository = observerRepository;
		this.webRTCStatDTOSink = this.makeSink(sampleSink);
		this.evaluators = evaluators;
	}

	@OnOpen
	public void onOpen(UUID observerUUID, WebSocketSession session) {
		Optional<ObserverDTO> observerDTO = observerRepository.findById(observerUUID);
		if (!observerDTO.isPresent()) {
			System.out.println("observer has not been found");
			session.close();
			return;
		}
	}

	private Random random = new Random();
	//String array
	String[] strings = {"First", "Second", "Third", "Forth", "Fifth", "Sixth", "Seventh", "Eight", "Ninth", "Tenth"};

	@OnMessage
	public void onMessage(
			UUID observerUUID,
			WebRTCStatDTO measurement,
			WebSocketSession session) {

//		DemoWebRTCStat stat = new DemoWebRTCStat();
//		stat.setBar(this.random.nextInt());
//		stat.setBaz(stat.getBar() % 2 == 0);
//		stat.setFoo(strings[this.random.nextInt(strings.length)]);
		this.webRTCStatDTOSink.accept(measurement);
		this.evaluators.accept(measurement);
	}

	@OnClose
	public void onClose(
			UUID observerUUID,
			WebSocketSession session) {
	}


	private Consumer<WebRTCStatDTO> makeSink(DemoSampleSink kafkaSink) {
		boolean enabled = true;
		Consumer<WebRTCStatDTO> result;
		if (enabled) {
			result = new Consumer<WebRTCStatDTO>() {
				private DemoSampleSink sink = kafkaSink;

				@Override
				public void accept(WebRTCStatDTO webRTCStatDTO) {
					this.sink.send(webRTCStatDTO);
				}
			};
		} else {
			result = stat -> {
			};
		}
		return result;
	}
}
