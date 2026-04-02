package com.pinkkila.authorizationserver.userid;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.uuid.Generators;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.UUID;

public record UserId(@JsonValue @NonNull UUID value) {
    
    public UserId {
        Objects.requireNonNull(value, "User ID cannot be null");
    }
    
    public static UserId generate() {
        return new UserId(Generators.timeBasedEpochGenerator().generate());
    }
    
    public static UserId fromString(String value) {
        return new UserId(UUID.fromString(value));
    }
    
    @Override
    public @NonNull String toString() {
        return value.toString();
    }
    
}
