package com.pinkkila.authorizationserver.appuser;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends CrudRepository<AppUser, UUID> {
    Optional<AppUser> findByUsername(String username);
}
