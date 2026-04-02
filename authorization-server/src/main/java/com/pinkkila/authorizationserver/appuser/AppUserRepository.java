package com.pinkkila.authorizationserver.appuser;

import com.pinkkila.authorizationserver.userid.UserId;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AppUserRepository extends CrudRepository<AppUser, UserId> {
    Optional<AppUser> findByUsername(String username);
}
