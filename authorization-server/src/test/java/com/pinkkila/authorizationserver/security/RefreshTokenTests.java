package com.pinkkila.authorizationserver.security;

import com.pinkkila.authorizationserver.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static com.pinkkila.authorizationserver.security.AuthorizationCodeFlow.CLIENT_ID;
import static com.pinkkila.authorizationserver.security.AuthorizationCodeFlow.CLIENT_SECRET;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DisplayName("Refresh token")
class RefreshTokenTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return a new access token and the same refresh token, so the refresh token's expiry doesn't move")
    void refresh_ReturnsNewAccessTokenAndSameRefreshToken() throws Exception {
        AuthorizationCodeFlow.Tokens tokens = new AuthorizationCodeFlow(this.mockMvc).obtainTokens();

        this.mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(CLIENT_ID, CLIENT_SECRET))
                        .param("grant_type", "refresh_token")
                        .param("refresh_token", tokens.refreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(not(tokens.accessToken())))
                .andExpect(jsonPath("$.refresh_token").value(tokens.refreshToken()));
    }

}
