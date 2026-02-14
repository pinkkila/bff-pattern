package com.pinkkila.resourceserver.userid;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.UUID;

@WritingConverter
public class UserIdWritingConverter implements Converter<UserId, UUID> {
    
    @Override
    public UUID convert(UserId source) {
        return source.value();
    }
    
}
