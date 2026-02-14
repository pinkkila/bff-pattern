package com.pinkkila.resourceserver.userid;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.UUID;

@ReadingConverter
public class UserIdReadingConverter implements Converter<UUID, UserId> {
    
    @Override
    public UserId convert(UUID source) {
        return new UserId(source);
    }

}
