package com.ssafy.ssamusoChat.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IdentificationDto {

    private String username;

    private String routingKey;

    public IdentificationDto(String username) {
        this.username = username;
    }
}
