package com.pinkkila.resourceserver.message;

import com.pinkkila.resourceserver.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/messages")
public class MessageController {
    
    private final MessageService messageService;
    
    // Todo: refactor pageable to use MessageQuery with @Valid
    @GetMapping
    public ResponseEntity<Page<MessageResponse>> getMessages(@CurrentUser UUID userId, Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessagesByUserId(userId, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessageByIdAndUserId(@CurrentUser UUID userId, @PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessageByIdAndUserId(id, userId));
    }
    
    @PostMapping
    public ResponseEntity<MessageResponse> createMessage(@CurrentUser UUID userId, @RequestBody @Valid MessageRequest messageRequest, UriComponentsBuilder ucb) {
        MessageResponse createdMessage = messageService.createMessage(userId, messageRequest);
        URI locationOfCreatedMessage = ucb
                .path("/messages/{id}")
                .buildAndExpand(createdMessage.id())
                .toUri();
        return ResponseEntity.created(locationOfCreatedMessage).body(createdMessage);
    }
    
}
