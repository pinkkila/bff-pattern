package com.pinkkila.resourceserver.message;

import com.pinkkila.resourceserver.security.JwtTestHelper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.pinkkila.resourceserver.TestcontainersConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

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
    void tearDown() {
        messageRepository.deleteAll();
    }
    
    @Nested
    @DisplayName("GET /messages")
    class GetMessagesTests {
        
        @Test
        @DisplayName("Should return successfully Page of Messages with 200")
        void  getMessages_SuccessDefault_Returns200() throws Exception {
           messageRepository.save(new Message(null, "Test Message", "test-user-id"));
           String token = JwtTestHelper.generateToken("test-user-id", List.of("message:read"));
           
           webTestClient.get()
                   .uri("/messages")
                   .header("Authorization", "Bearer " + token)
                   .exchange()
                   .expectStatus().isOk();
        }
        
    }

}
