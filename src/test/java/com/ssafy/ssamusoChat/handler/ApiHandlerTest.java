package com.ssafy.ssamusoChat.handler;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class ApiHandlerTest {

    @Test
    void webClientTest() {
        WebClient webClient = WebClient.create("https://jsonplaceholder.typicode.com");
        ResponseEntity<String> responseEntity = webClient.get()
                .uri("/users")
                .exchangeToMono(response -> response.toEntity(String.class))
                .doOnSuccess(response -> {
                    if (!response.getStatusCode().equals(HttpStatus.OK)) {
                        throw new RuntimeException("api 호출 실패");
                    }
                })
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.UNAUTHORIZED)))
                .block();
        String body = responseEntity != null ? responseEntity.getBody() : null;
        Assertions.assertThat(body).isInstanceOf(String.class);
    }
}