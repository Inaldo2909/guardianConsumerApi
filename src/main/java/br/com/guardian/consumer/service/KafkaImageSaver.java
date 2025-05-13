package br.com.guardian.consumer.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KafkaImageSaver {

    private static final String OUTPUT_DIR = "src/main/resources/photos/receiver/";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "topic.frame.processado", groupId = "file-saver-group")
    public void saveImageFromKafka(ConsumerRecord<String, String> record) {
        try {
            JsonNode json = objectMapper.readTree(record.value());
            String base64Image = json.get("frame").asText();

            byte[] imageBytes = Base64.decodeBase64(base64Image);
            Files.createDirectories(Paths.get(OUTPUT_DIR));

            String filename = "frame_" + LocalDateTime.now().format(FORMATTER) + ".jpg";
            File imageFile = new File(OUTPUT_DIR + filename);

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(imageBytes);
                System.out.println("✅ Imagem salva em: " + imageFile.getAbsolutePath());
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao salvar imagem: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

