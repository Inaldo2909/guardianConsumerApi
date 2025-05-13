package br.com.guardian.consumer.service;

import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executors;

@Service
public class ImageWatcherProducer {

    private static final String WATCH_DIR = "src/main/resources/photos/to_send";
    private static final String TOPIC = "topic.frame.processado";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostConstruct
    public void startWatching() {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Path dir = Paths.get(WATCH_DIR);
                Files.createDirectories(dir);
                WatchService watchService = FileSystems.getDefault().newWatchService();
                dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                System.out.println("👀 Monitorando pasta: " + dir.toAbsolutePath());

                while (true) {
                    WatchKey key = watchService.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            Path filename = (Path) event.context();
                            File file = dir.resolve(filename).toFile();

                            // Pequeno atraso para garantir que o arquivo está pronto
                            Thread.sleep(500);

                            byte[] bytes = Files.readAllBytes(file.toPath());
                            String base64 = Base64.encodeBase64String(bytes);

                            String json = String.format("{\"frame\":\"%s\"}", base64);
                            kafkaTemplate.send(TOPIC, json);

                            System.out.println("📤 Imagem enviada para Kafka: " + file.getName());

                            // ✅ Apaga o arquivo após envio
                            if (file.delete()) {
                                System.out.println("🗑️  Imagem deletada: " + file.getName());
                            } else {
                                System.err.println("⚠️  Falha ao deletar imagem: " + file.getName());
                            }
                        }
                    }

                    key.reset();
                }

            } catch (IOException | InterruptedException e) {
                System.err.println("❌ Erro no watcher: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
