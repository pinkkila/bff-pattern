package com.pinkkila.resourceserver.message;

import com.pinkkila.resourceserver.userid.UserId;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@AllArgsConstructor
@Table("message")
public class Message {
    @Id
    private Long id;
    private String content;
    private UserId userId;
}
