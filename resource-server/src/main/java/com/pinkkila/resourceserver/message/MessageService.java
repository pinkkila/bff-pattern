package com.pinkkila.resourceserver.message;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesByUserId(String userId, Pageable pageable) {
        Page<Message> messagesPage = messageRepository.findByUserId(userId, pageable);
        return messagesPage.map(this::mapToResponse);
    }
    
    public MessageResponse createMessage(String userId, MessageRequest messageRequest) {
        Message message = new Message(null, messageRequest.content(), userId);
        return mapToResponse(messageRepository.save(message));
    }
    
    private MessageResponse mapToResponse(Message message) {
        return new MessageResponse(message.getId(), message.getContent());
    }
    
}
