package com.pinkkila.resourceserver.message;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/messages")
public class MessageController {
    
    private final MessageService messageService;
    
    // Todo: refactor pageable to use MessageQuery with @Valid
    @GetMapping
    public ResponseEntity<Page<MessageResponse>> getMessages(Authentication authentication, Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessagesByUserId(authentication.getName(), pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessageByIdAndUserId(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessageByIdAndUserId(id, authentication.getName()));
    }
    
    @PostMapping
    public ResponseEntity<MessageResponse> createMessage(Authentication authentication, @RequestBody @Valid MessageRequest messageRequest, UriComponentsBuilder ucb) {
        MessageResponse createdMessage = messageService.createMessage(authentication.getName(), messageRequest);
        URI locationOfCreatedMessage = ucb
                .path("/messages/{id}")
                .buildAndExpand(createdMessage.id())
                .toUri();
        return ResponseEntity.created(locationOfCreatedMessage).body(createdMessage);
    }
    
}
