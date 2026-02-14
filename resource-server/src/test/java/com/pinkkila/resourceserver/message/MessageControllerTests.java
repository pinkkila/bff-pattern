package com.pinkkila.resourceserver.message;

import com.pinkkila.resourceserver.security.SecurityConfig;
import com.pinkkila.resourceserver.userid.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@Import(SecurityConfig.class)
@DisplayName("Message Controller Tests")
public class MessageControllerTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    MessageService messageService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Nested
    @DisplayName("GET /messages")
    class GetMessagesTests {
        
        @Test
        @DisplayName("Should return successfully Page of Messages with 200")
        void getMessages_SuccessDefault_Returns200() throws Exception {
            UserId userId = new UserId(UUID.randomUUID());
            MessageResponse res1 = new MessageResponse(1L, "Test message!");
            Page<MessageResponse> page = new PageImpl<>(List.of(res1), PageRequest.of(0, 20), 1);
            
            when(messageService.getMessagesByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(page);
            
            String expectedJson = """
                    {
                        "content": [
                            {
                                "id": 1,
                                "content": "Test message!"
                            }
                        ],
                        "page": {
                            "size":20,
                            "number":0,
                            "totalElements":1,
                            "totalPages":1
                        }
                    }
                    """;
            
            mockMvc.perform(get("/messages")
                            .with(jwt()
                                    .jwt((jwt) -> jwt.subject(userId.toString())
                                            .claim("scope", "message:read")
                                    )
                            ))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedJson, JsonCompareMode.STRICT));
            
            verify(messageService).getMessagesByUserId(eq(userId), any(Pageable.class));
        }
        
        @Test
        @DisplayName("Should return unauthenticated when there is no JWT with 401")
        void getMessages_NoJwt_Returns401() throws Exception {
            mockMvc.perform(get("/messages"))
                    .andExpect(status().isUnauthorized());
            
            verifyNoInteractions(messageService);
        }
        
        @Test
        @DisplayName("Should return forbidden when there is no scope message:read with 403")
        void getMessages_NoScopeRead_Returns403() throws Exception {
            UserId userId = new UserId(UUID.randomUUID());
            mockMvc.perform(get("/messages")
                            .with(jwt()
                                    .jwt((jwt) -> jwt.subject(userId.toString())
                                    )
                            ))
//                    .andDo(print())
                    .andExpect(status().isForbidden());
            
            verifyNoInteractions(messageService);
        }
        
        
        @Test
        @DisplayName("Should return empty page when user has no messages with 200")
        void getMessages_DifferentSubReturnsEmptyPage_Returns200() throws Exception {
            UserId userId = new UserId(UUID.randomUUID());
            UserId otherUserId = new UserId(UUID.randomUUID());
            when(messageService.getMessagesByUserId(eq(otherUserId), any(Pageable.class)))
                    .thenReturn(Page.empty());
            
            String expectedJson = """
                    {
                        "content": [],
                        "page": {
                            "size":0,
                            "number":0,
                            "totalElements":0,
                            "totalPages":1
                        }
                    }
                    """;
            
            mockMvc.perform(get("/messages")
                            .with(jwt()
                                    .jwt(jwt -> jwt.subject(otherUserId.toString())
                                            .claim("scope", "message:read")
                                    )
                            ))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedJson, JsonCompareMode.STRICT));
            
            verify(messageService).getMessagesByUserId(eq(otherUserId), any(Pageable.class));
            verify(messageService, times(0)).getMessagesByUserId(eq(userId), any());
            
        }
        
    }
    
    @Nested
    @DisplayName("POST /messages")
    class PostMessageTests {
        
        @Test
        @DisplayName("Successful creation should return 201 Created with response body")
        void createMessage_Success_Returns201() throws Exception {
            UserId userId = new UserId(UUID.randomUUID());
            String content = "Test message";
            MessageRequest request = new MessageRequest(content);
            MessageResponse response = new MessageResponse(1L, content);
            
            when(messageService.createMessage(eq(userId), eq(request))).thenReturn(response);
            
            mockMvc.perform(post("/messages")
                            .with(jwt()
                                    .jwt(jwt -> jwt.subject(userId.toString())
                                            .claim("scope", "message:write")
                                    )
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/messages/1"))
                    .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
            
            verify(messageService).createMessage(eq(userId), eq(request));
        }
        
        @Test
        @DisplayName("Should return unauthenticated when there is no JWT with 401")
        void createMessages_NoJwt_Returns401() throws Exception {
            String content = "Test message";
            MessageRequest request = new MessageRequest(content);
            
            mockMvc.perform(post("/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isUnauthorized());
            
            verifyNoInteractions(messageService);
        }
        
        @Test
        @DisplayName("Should return forbidden when there is no scope message:write with 403")
        void createMessage_NoScopeWrite_Returns403() throws Exception {
            UserId userId = new UserId(UUID.randomUUID());
//            UUID userId = UUID.randomUUID();
            String content = "Test message";
            MessageRequest request = new MessageRequest(content);
            
            mockMvc.perform(post("/messages")
                            .with(jwt()
                                    .jwt((jwt) -> jwt.subject(userId.toString())
                                    )
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden());
            
            verifyNoInteractions(messageService);
        }
        
    }
    
}
