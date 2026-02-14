package com.pinkkila.resourceserver.message;

import com.pinkkila.resourceserver.security.JwtTestHelper;
import com.pinkkila.resourceserver.userid.UserId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.pinkkila.resourceserver.TestcontainersConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Message Integration Tests")
public class MessageFullIntegrationTests {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
    }
    
    @Nested
    @DisplayName("GET /messages")
    class GetMessagesTests {
        
        @Test
        @DisplayName("Should return successfully Page of Messages with 200")
        void getMessages_SuccessDefault_Returns200() throws Exception {
            UserId userId = new UserId(UUID.randomUUID());
            messageRepository.save(new Message(null, "Test Message", userId));
            String token = JwtTestHelper.generateToken(userId.toString(), List.of("message:read"));
            
            webTestClient.get()
                    .uri("/messages")
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus().isOk();
        }
        
    }
    
    @Nested
    @DisplayName("GET /messages/{id}")
    class GetMessageByIdTests {
        
        @Test
        @DisplayName("Should return successfully requested message with 200")
        void getMessage_Success_Returns200() throws Exception {
            UserId userId = new UserId(UUID.randomUUID());
            Message savedMessage = messageRepository.save(new Message(null, "Test Message", userId));
            String token = JwtTestHelper.generateToken(userId.toString(), List.of("message:read"));
            
            webTestClient.get()
                    .uri("/messages/" + savedMessage.getId())
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus().isOk();
        }
        
        @Test
        @DisplayName("Should return MessageNotFound when userId is wrong with 404")
        void getMessage_WrongUserId_Returns404() throws Exception {
            UserId userId = new UserId(UUID.randomUUID());
            UserId otherUserId = new UserId(UUID.randomUUID());
            Message savedMessage = messageRepository.save(new Message(null, "Test Message", otherUserId));
            String token = JwtTestHelper.generateToken(userId.toString(), List.of("message:read"));
            
            webTestClient.get()
                    .uri("/messages/" + savedMessage.getId())
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus().isNotFound();
        }
        
    }
    
}
