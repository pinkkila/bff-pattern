package com.pinkkila.bff;

import com.pinkkila.bff.security.TestClientRegistrationConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestClientRegistrationConfig.class)
class BffApplicationTests {
    
    @Test
    void contextLoads() {
    }
    
}
