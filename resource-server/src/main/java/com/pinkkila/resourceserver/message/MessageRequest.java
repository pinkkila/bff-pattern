package com.pinkkila.resourceserver.message;

import lombok.NonNull;

public record MessageRequest(
        @NonNull
        String content
) {
}
