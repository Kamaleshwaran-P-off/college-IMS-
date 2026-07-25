package com.smartcampus.platform.auth.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;

@Configuration
public class DefaultUserSeeder {

  @Bean
  public CommandLineRunner initDefaultUsers(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder
  ) {
    return args -> {
      createIfMissing(
          userRepository,
          passwordEncoder,
          "student@test.com",
          "student123",
          Role.STUDENT,
          "Student User"
      );

      // "Faculty" maps to STAFF role in this app.
      createIfMissing(
          userRepository,
          passwordEncoder,
          "faculty@test.com",
          "faculty123",
          Role.STAFF,
          "Faculty User"
      );

      createIfMissing(
          userRepository,
          passwordEncoder,
          "admin@test.com",
          "admin123",
          Role.ADMIN,
          "Admin User"
      );
    };
  }

  private void createIfMissing(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      String email,
      String rawPassword,
      Role role,
      String fullName
  ) {
    if (userRepository.existsByEmail(email)) {
      return;
    }
    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setRole(role);
    user.setFullName(fullName);
    userRepository.save(user);
  }
}
