package com.pinkkila.authorizationserver.appuser;

import com.pinkkila.authorizationserver.appuser.exception.AppUserNotFoundException;
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
    
        public void changeUsername(UserId userId, String newUsername) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new AppUserNotFoundException("UserNotFound"));

        AppUser updatedUser = user.withUsername(newUsername);

        userRepository.save(updatedUser);
    }
    
//    public void changePassword(UserId userId, String newRawPassword) {
//        AppUser user = userRepository.findById(userId)
//                .orElseThrow(() -> new UserNotFoundException(userId));
//
//        // Create the updated instance
//        AppUser updatedUser = user.withPassword(passwordEncoder.encode(newRawPassword));
//
//        // Save the new instance (Spring Data JDBC performs an UPDATE)
//        userRepository.save(updatedUser);
//    }
}
