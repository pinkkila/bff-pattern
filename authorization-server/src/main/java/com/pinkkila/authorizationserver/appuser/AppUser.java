package com.pinkkila.authorizationserver.appuser;

import com.pinkkila.authorizationserver.userid.UserId;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("app_user")
public class AppUser implements Persistable<UserId> {
    @Id
    private final UserId id;
    private final String username;
    private final String password;
    
    @Transient
    private final boolean isNew;
    
    @PersistenceCreator
    public AppUser(UserId id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.isNew = false;
    }
    
    private AppUser(UserId id, String username, String password, boolean isNew) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.isNew = isNew;
    }
    
    public static AppUser create(String username, String encodedPassword) {
        return new AppUser(UserId.generate(), username, encodedPassword, true);
    }
    
    public AppUser withUsername(String newUsername) {
        return new AppUser(this.id, newUsername, this.password, false);
    }
    
    public AppUser withPassword(String newEncodedPassword) {
        return new AppUser(this.id, this.username, newEncodedPassword, false);
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
