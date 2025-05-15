package br.com.guardian.consumer.controller;

import br.com.guardian.consumer.model.Message;
import br.com.guardian.consumer.service.KafkaMessageConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api")
@RestController
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private KafkaMessageConsumer consumer;

    @KafkaListener(topics = "topic.frame.processado", groupId = "websocket-group")
    public void listen(String message) {
        System.out.println("Mensagem recebida do Kafka: " + message);
        messagingTemplate.convertAndSend("/topic/frames", message);
    }

    @GetMapping("/messages/{timeout}")
    public List<Message> getLatestKafkaMessages(long timeout) {
        return consumer.pollMessages(timeout);
    }


}
