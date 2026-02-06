package com.pinkkila.resourceserver.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@Table("message")
public class Message {
    @Id
    private Long id;
    private String content;
    private String userId;
}
