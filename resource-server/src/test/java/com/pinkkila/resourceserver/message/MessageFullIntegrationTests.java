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
    
    String messageReadToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdWJqZWN0IiwiZXhwIjoyMTY0MjQ1NjQ4LCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiY2I1ZGMwNDYtMDkyMi00ZGJmLWE5MzAtOGI2M2FhZTYzZjk2IiwiY2xpZW50X2lkIjoicmVhZGVyIiwic2NvcGUiOlsibWVzc2FnZTpyZWFkIl19.Pre2ksnMiOGYWQtuIgHB0i3uTnNzD0SMFM34iyQJHK5RLlSjge08s9qHdx6uv5cZ4gZm_cB1D6f4-fLx76bCblK6mVcabbR74w_eCdSBXNXuqG-HNrOYYmmx5iJtdwx5fXPmF8TyVzsq_LvRm_LN4lWNYquT4y36Tox6ZD3feYxXvHQ3XyZn9mVKnlzv-GCwkBohCR3yPow5uVmr04qh_al52VIwKMrvJBr44igr4fTZmzwRAZmQw5rZeyep0b4nsCjadNcndHtMtYKNVuG5zbDLsB7GGvilcI9TDDnUXtwthB_3iq32DAd9x8wJmJ5K8gmX6GjZFtYzKk_zEboXoQ";
    
    
    @Nested
    @DisplayName("GET /messages")
    class GetMessagesTests {
        
        @Test
        @DisplayName("Should return successfully Page of Messages with 200")
        void  getMessages_SuccessDefault_Returns200() throws Exception {
           messageRepository.save(new Message(null, "Test Message", "test-user-id"));
//           String token = JwtTestHelper.generateToken("test-user-id", List.of("message:read"));
           
           webTestClient.get()
                   .uri("/messages")
                   .header("Authorization", "Bearer " + messageReadToken)
                   .exchange()
                   .expectStatus().isOk();
        }
        
    }

}
