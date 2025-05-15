package br.com.guardian.consumer.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class Message {

    private Long offset;
    private LocalDateTime timestamp;
    private String key;
    private String value;

    public Message(Long offset, LocalDateTime timestamp, String key, String value) {
        this.offset = offset;
        this.timestamp = timestamp;
        this.key = key;
        this.value = value;
    }

}
