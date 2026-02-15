package github.maxsuel.agregadordeinvestimentos.service;

import github.maxsuel.agregadordeinvestimentos.config.TokenService;
import github.maxsuel.agregadordeinvestimentos.dto.request.auth.CreateUserDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.auth.LoginDto;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.entity.enums.Role;
import github.maxsuel.agregadordeinvestimentos.exceptions.DuplicatedDataException;
import github.maxsuel.agregadordeinvestimentos.mapper.UserMapper;
import github.maxsuel.agregadordeinvestimentos.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BlacklistService blacklistService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Tests for Register.")
    public class RegisterTests {

        @Test
        @DisplayName("Should register user with hashed password.")
        public void shouldRegisterUserWithSuccess() {
            // Arrange & Act
            var dto = new CreateUserDto("username", "user@email.com", "plainPassword");
            var user = new User();
            user.setUserId(UUID.randomUUID());

            when(userRepository.existsByUsername(dto.username())).thenReturn(false);
            when(userRepository.existsByEmail(dto.email())).thenReturn(false);
            when(passwordEncoder.encode(dto.password())).thenReturn("hashed");
            when(userMapper.toEntity(eq(dto), anyString())).thenReturn(user);
            when(userRepository.save(any())).thenReturn(user);
            when(tokenService.generateToken(any())).thenReturn("token-123");

            var result = authService.register(dto);

            // Assert
            assertNotNull(result);
            assertEquals("token-123", result.accessToken());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw DuplicatedDataException when username exists.")
        public void shouldThrowExceptionWhenUsernameExists() {
            var dto = new CreateUserDto("existingUser", "user@email.com", "pass");
            when(userRepository.existsByUsername(dto.username())).thenReturn(true);

            assertThrows(DuplicatedDataException.class, () -> authService.register(dto));
            verify(userRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("Tests for Login")
    public class LoginTests {

        @Test
        @DisplayName("Should login and return token when credentials are valid")
        public void shouldLoginWithSuccess() {
            // Arrange
            var dto = new LoginDto("user", "plainPassword");
            var user = new User("user", "user@email.com", "hashedPassword", Role.ADMIN);
            var expectedToken = "token-jwt-123";

            when(userRepository.findByUsername(dto.username())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(true);
            when(tokenService.generateToken(user)).thenReturn(expectedToken);

            // Act
            var result = authService.login(dto);

            // Assert
            assertEquals(expectedToken, result.accessToken());
            verify(tokenService).generateToken(user);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when password does not match")
        public void shouldThrowExceptionWhenPasswordInvalid() {
            // Arrange
            var dto = new LoginDto("nonExistent", "anyPass");
            when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(BadCredentialsException.class, () -> authService.login(dto));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        public void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            var dto = new LoginDto("nonExistent", "anyPass");
            when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(BadCredentialsException.class, () -> authService.login(dto));
        }

    }

    @Nested
    @DisplayName("Logout & Session Tests.")
    public class SessionTests {

        @Test
        @DisplayName("Should add token to blacklist on logout.")
        public void shouldLogoutWithSuccess() {
            // Arrange & Act
            String header = "Bearer my-token";

            authService.logout(header);

            // Assert
            verify(blacklistService).blacklistToken("my-token");
        }

        @Test
        @DisplayName("Should throw exception when authenticated user is null.")
        public void shouldThrowExceptionWhenUserIsNull() {
            // Arrange, Act & Assert
            assertThrows(BadCredentialsException.class, () -> authService.getAuthenticatedUserDto(null));
        }

    }

}
