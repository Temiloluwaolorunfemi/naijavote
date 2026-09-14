package com.naijavote.service;

import com.naijavote.dto.RegisterRequest;
import com.naijavote.entity.Role;
import com.naijavote.entity.User;
import com.naijavote.repository.RoleRepository;
import com.naijavote.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUserCreatesUserWithEncodedPasswordAndUserRole() {
        RegisterRequest request = new RegisterRequest("ada", "ada@example.com", "plain", "plain");
        Role role = new Role();
        role.setName("USER");
        User savedUser = new User();

        when(userRepository.existsByUsername("ada")).thenReturn(false);
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(request);

        assertEquals(savedUser, result);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("plain");
    }

    @Test
    void registerUserRejectsDuplicateUsername() {
        RegisterRequest request = new RegisterRequest("ada", "ada@example.com", "plain", "plain");
        when(userRepository.existsByUsername("ada")).thenReturn(true);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(request));

        assertEquals("Username already exists", error.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserRequiresUserRole() {
        RegisterRequest request = new RegisterRequest("ada", "ada@example.com", "plain", "plain");
        when(userRepository.existsByUsername("ada")).thenReturn(false);
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> userService.registerUser(request));

        assertEquals("USER role not found", error.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByUsernameReturnsMatchingUser() {
        User user = new User();
        when(userRepository.findByUsername("ada")).thenReturn(Optional.of(user));

        assertInstanceOf(User.class, userService.getUserByUsername("ada"));
    }

    @Test
    void getUserByUsernameRejectsUnknownUser() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> userService.getUserByUsername("missing"));

        assertEquals("User not found", error.getMessage());
    }
}
