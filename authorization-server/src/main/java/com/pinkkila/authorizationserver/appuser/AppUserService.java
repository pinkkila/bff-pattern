package com.pinkkila.authorizationserver.appuser;

import com.pinkkila.authorizationserver.userid.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository userRepository;
//    private final PasswordEnoder passwordEncoder;
    
    @Transactional
    public UserId registerUser(String username, String rawPassword) {
        AppUser newUser = AppUser.create(username, "{noop}" + rawPassword);
//        AppUser newUser = AppUser.create(username, passwordEncoder.encode(rawPassword));
        return userRepository.save(newUser).getId();
    }
}
