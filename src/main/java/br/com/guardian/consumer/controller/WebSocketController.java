package br.com.guardian.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "topic.frame.processado", groupId = "websocket-group")
    public void listen(String message) {
        System.out.println("Mensagem recebida do Kafka: " + message);
        messagingTemplate.convertAndSend("/topic/frames", message);
    }
}
