package com.smartcampus.platform.auth.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository
        .findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    String roleName = user.getRole().name();
    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
    // Backward-compatible alias: treat STAFF as FACULTY for /api/faculty/** rules.
    if ("STAFF".equals(roleName)) {
      authorities.add(new SimpleGrantedAuthority("ROLE_FACULTY"));
    }

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPassword(),
        authorities
    );
  }
}
