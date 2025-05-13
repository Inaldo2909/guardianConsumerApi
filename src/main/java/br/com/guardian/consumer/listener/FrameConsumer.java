package br.com.guardian.consumer.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class FrameConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    public FrameConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "topic.frame.processado", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        System.out.println("Kafka Received: " + message);
        messagingTemplate.convertAndSend("/topic/frames", message);
    }
}
