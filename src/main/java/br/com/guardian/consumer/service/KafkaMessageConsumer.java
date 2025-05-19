package br.com.guardian.consumer.service;

import br.com.guardian.consumer.model.Message;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;


@Service
public class KafkaMessageConsumer {
    private static final String TOPIC = "topic.frame.processado";
    private final KafkaConsumer<String, String> consumer;

    public KafkaMessageConsumer(@Value("${spring.kafka.bootstrap-servers}")
                                String bootstrapServers, @Value("{spring.kafka.bootstrap-servers}") String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Opcional: configurar para ler do início do tópico se o groupId for novo
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Opcional: desabilitar auto commit se você quiser controlar o commit manualmente
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(TOPIC));
        System.out.println("Consumidor Kafka configurado para o tópico: " + TOPIC);
    }

    /**
     * Busca mensagens do tópico Kafka.
     * @param timeout O tempo máximo em milissegundos para esperar por registros.
     * @return Uma lista de objetos Message preenchidos.
     */
    public List<Message> pollMessages(long timeout) {
        List<Message> messages = new ArrayList<>();
        try {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(timeout));
            for (ConsumerRecord<String, String> record : records) {
                Message message = Message.builder()
                        .offset(record.offset())
                        .timestamp(LocalDateTime.ofEpochSecond(record.timestamp() / 1000, (int) (record.timestamp() % 1000) * 1_000_000, ZoneOffset.UTC)) // Converte epoch millis para LocalDateTime
                        .key(record.key())
                        .value(record.value())
                        .build();
                messages.add(message);
                System.out.println("Mensagem recebida: " + message);
            }
            // Commita os offsets processados (se enable.auto.commit for false)
            consumer.commitSync();
        } catch (Exception e) {
            System.err.println("Erro ao buscar mensagens do Kafka: " + e.getMessage());
            e.printStackTrace();
        }
        close();
        return messages;
    }

    public void close() {
        consumer.close();
        System.out.println("Consumidor Kafka fechado.");
    }

}
