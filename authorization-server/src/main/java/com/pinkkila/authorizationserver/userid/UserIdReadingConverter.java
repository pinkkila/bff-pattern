package com.pinkkila.authorizationserver.userid;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.UUID;

@ReadingConverter
public class UserIdReadingConverter implements Converter<UUID, UserId> {
    
    @Override
    public UserId convert(@NonNull UUID source) {
        return new UserId(source);
    }
    
}
