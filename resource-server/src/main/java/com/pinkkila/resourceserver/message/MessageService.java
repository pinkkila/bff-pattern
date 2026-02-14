package com.pinkkila.resourceserver.message;

import com.pinkkila.resourceserver.message.exception.MessageNotFound;
import com.pinkkila.resourceserver.userid.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesByUserId(UserId userId, Pageable pageable) {
        Page<Message> messagesPage = messageRepository
                .findByUserId(userId, pageable);
        return messagesPage.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public MessageResponse getMessageByIdAndUserId(Long id, UserId userId) {
        log.info("Getting message for user with userId: {}", userId);
        return messageRepository.findByIdAndUserId(id, userId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new MessageNotFound("Message not found with id: " + id));
    }
    
    @Transactional
    public MessageResponse createMessage(UserId userId, MessageRequest messageRequest) {
        Message message = new Message(null, messageRequest.content(), userId);
        return mapToResponse(messageRepository.save(message));
    }
    
    private MessageResponse mapToResponse(Message message) {
        return new MessageResponse(message.getId(), message.getContent());
    }
    
}
