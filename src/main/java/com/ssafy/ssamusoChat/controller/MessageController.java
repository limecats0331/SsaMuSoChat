package com.ssafy.ssamusoChat.controller;

import com.ssafy.ssamusoChat.dto.IdentificationDto;
import com.ssafy.ssamusoChat.handler.ApiHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    @Value("${rabbitmq.exchangeName}")
    private String exchangeName;

    private final RabbitTemplate rabbitTemplate;

    private final ApiHandler apiHandler;

    @MessageMapping("room.{roomKey}")
    public void messsage(Message<?> message, @DestinationVariable String roomKey, @Payload IdentificationDto identificationDto) {
        log.info("{}", identificationDto);
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() == StompCommand.SUBSCRIBE && !apiHandler.identify(identificationDto)) {
            return;
        }
        rabbitTemplate.convertAndSend(exchangeName, "room." + roomKey, identificationDto);
    }

}
