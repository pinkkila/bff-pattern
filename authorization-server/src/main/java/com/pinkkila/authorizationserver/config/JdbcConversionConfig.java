package com.pinkkila.authorizationserver.config;

import com.pinkkila.authorizationserver.userid.UserIdReadingConverter;
import com.pinkkila.authorizationserver.userid.UserIdWritingConverter;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class JdbcConversionConfig extends AbstractJdbcConfiguration {
    
    @Override
    protected @NonNull List<?> userConverters() {
        return Arrays.asList(new UserIdWritingConverter(), new UserIdReadingConverter());
    }
    
}
