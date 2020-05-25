package com.observertc.gatekeeper;

import com.observertc.gatekeeper.dto.DemoWebRTCStat;
import com.observertc.gatekeeper.dto.ObserverDTO;
import com.observertc.gatekeeper.models.DemoMeasurement;
import com.observertc.gatekeeper.repositories.ObserverRepository;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@ServerWebSocket("/ws/demo/{observerUUID}")
public class DemoWebsocketServer {

	private final ObserverRepository observerRepository;
	private final DemoSampleSink sink;
//	private final IDSLContextProvider contextProvider;

//	public DemoWebsocketServer(ObserverRepository observerRepository, DemoSink sink) {
//		this.observerRepository = observerRepository;
//		this.sink = sink;
//	}

	public DemoWebsocketServer(ObserverRepository observerRepository, DemoSampleSink sink) {
		this.observerRepository = observerRepository;
//		this.contextProvider = contextProvider;
		this.sink = sink;
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
			DemoMeasurement measurement,
			WebSocketSession session) {
		DemoWebRTCStat stat = new DemoWebRTCStat();
		stat.setBar(this.random.nextInt());
		stat.setBaz(stat.getBar() % 2 == 0);
		stat.setFoo(strings[this.random.nextInt(strings.length)]);
		sink.send(stat);
	}

	@OnClose
	public void onClose(
			UUID observerUUID,
			WebSocketSession session) {
	}

}
