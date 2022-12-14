package com.ssafy.ssamusoChat.handler;

import com.ssafy.ssamusoChat.dto.IdentificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ApiHandler {

    private final String ssamusoUrl;

    public ApiHandler(@Value("${ssamusoUrl}") String ssamusoUrl) {
        this.ssamusoUrl = ssamusoUrl;
    }

    public boolean identify(IdentificationDto identificationDto) {
        WebClient webClient = WebClient.create(ssamusoUrl);
        ResponseEntity<String> responseEntity = webClient.post()
                .uri("/users/identify")
                .body(Mono.just(identificationDto), IdentificationDto.class)
                .exchangeToMono(response -> response.toEntity(String.class))
                .doOnSuccess(response -> {
                    if (!response.getStatusCode().equals(HttpStatus.OK)) {
                        throw new RuntimeException("인가되지 않은 사용자");
                    }
                })
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.UNAUTHORIZED)))
                .block();
        return responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK);
    }
}
