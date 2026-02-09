package com.pinkkila.authorizationserver.appuser;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@AllArgsConstructor
@Table("app_user")
public class AppUser {
    @Id
    private UUID id;
    private String username;
    private String password;
}
