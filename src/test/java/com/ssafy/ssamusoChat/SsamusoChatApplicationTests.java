package com.ssafy.ssamusoChat;

import com.ssafy.ssamusoChat.dto.IdentificationDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SsamusoChatApplicationTests {

	@LocalServerPort
	private int port;

	private StompSession stompSession;


	private final String url;

	private BlockingQueue<IdentificationDto> queue = new LinkedBlockingDeque<>();

	private final WebSocketStompClient webSocketStompClient;

	public SsamusoChatApplicationTests() {
		StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
		WebSocketTransport webSocketTransport = new WebSocketTransport(standardWebSocketClient);
		List<Transport> transports = Collections.singletonList(webSocketTransport);
		SockJsClient sockJsClient = new SockJsClient(transports);
		this.webSocketStompClient = new WebSocketStompClient(sockJsClient);
		this.webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
		this.url = "ws://localhost:";
	}

	@BeforeEach
	public void connect() throws ExecutionException, InterruptedException, TimeoutException {
		this.stompSession = this.webSocketStompClient.connect(url + port + "/stomp", new StompSessionHandlerAdapter() {
				})
				.get(60, TimeUnit.SECONDS);
	}

	@AfterEach
	public void disconnect() {
		if (this.stompSession.isConnected()) {
			this.stompSession.disconnect();
		}
	}

	@Test
	void contextLoads() throws InterruptedException {
		stompSession.subscribe("/exchange/chatting.exchange/room.1", new StompFrameHandlerImpl());
		stompSession.send("/chatting/room.1", new IdentificationDto("username"));
		IdentificationDto dto = queue.poll(5, TimeUnit.SECONDS);

		assertThat(dto.getFrom()).isEqualTo("username");
	}

	private class StompFrameHandlerImpl implements StompFrameHandler {

		@Override
		public Type getPayloadType(StompHeaders headers) {
			return IdentificationDto.class;
		}

		@Override
		public void handleFrame(StompHeaders headers, Object payload) {
			queue.offer((IdentificationDto) payload);
		}
	}

}
