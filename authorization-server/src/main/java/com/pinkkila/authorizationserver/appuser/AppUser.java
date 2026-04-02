package com.pinkkila.authorizationserver.appuser;

import com.pinkkila.authorizationserver.userid.UserId;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("app_user")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser implements Persistable<UserId> {
    @Id
    private UserId id;
    private String username;
    private String password;
    
    @Transient
    private boolean isNew = false;
    
    public static AppUser create(String username, String encodedPassword) {
        return new AppUser(UserId.generate(), username, encodedPassword, true);
    }
    
    @Override
    public UserId getId() {
        return id;
    }
    
    @Override
    public boolean isNew() {
        return isNew;
    }
}
