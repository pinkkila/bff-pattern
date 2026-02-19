package com.pinkkila.authorizationserver.security;

import com.pinkkila.authorizationserver.appuser.AppUser;
import com.pinkkila.authorizationserver.appuser.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final AppUserRepository appUserRepository;
    
    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return this.appUserRepository.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("username " + username + " is not found"));
    }
    
    static final class CustomUserDetails extends AppUser implements UserDetails {
        
        private static final List<GrantedAuthority> ROLE_USER = Collections
                .unmodifiableList(AuthorityUtils.createAuthorityList("ROLE_USER"));
        
        CustomUserDetails(AppUser appUser) {
            super(appUser.getId(), appUser.getUsername(), appUser.getPassword());
        }
        
        @Override
        @NonNull
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return ROLE_USER;
        }
        
        @Override
        @NonNull
        public String getUsername() {
            return super.getUsername();
        }
        
    }
}
