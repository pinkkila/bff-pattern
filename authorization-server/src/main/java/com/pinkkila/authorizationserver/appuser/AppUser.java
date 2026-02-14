package com.pinkkila.authorizationserver.appuser;

import com.pinkkila.authorizationserver.userid.UserId;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@Table("app_user")
public class AppUser {
    @Id
    private UserId id;
    private String username;
    private String password;
}
