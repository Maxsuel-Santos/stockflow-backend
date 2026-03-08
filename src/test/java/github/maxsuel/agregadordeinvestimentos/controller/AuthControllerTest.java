package github.maxsuel.agregadordeinvestimentos.controller;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import github.maxsuel.agregadordeinvestimentos.dto.request.auth.CreateUserDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.auth.LoginDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.auth.AuthResponseDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.auth.UserDto;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.entity.enums.Role;
import github.maxsuel.agregadordeinvestimentos.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    @Nested
    @DisplayName("Tests for Create/Register User.")
    public class RegisterTests {

        @Test
        @DisplayName("Should return 201 Created on success.")
        public void shouldCreateUserWithSuccess() {
            // Arrange
            var dto = new CreateUserDto("username", "username@email.com", "123");
            var userId = UUID.randomUUID().toString();
            var userDto = new UserDto(
                userId, 
                "username", 
                "username@email.com", 
                Role.ADMIN, 
                "http://example.com/avatar.jpg"
            );
            var userResponse = new AuthResponseDto("fake-jwt-token", userDto);

            when(authService.register(dto)).thenReturn(userResponse);

            // Act
            var response = authController.register(dto);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getHeaders().getLocation());

            assertTrue(response.getHeaders().getLocation().getPath().contains(userId));
        }

        @Nested
        @DisplayName("Tests for Login.")
        public class LoginTests {

            @Test
            @DisplayName("Should return 200 OK and JWT token on success.")
            public void shouldLoginWithSuccess() {
                // Arrange
                var loginDto = new LoginDto("username", "123");
                var userResponse = new AuthResponseDto("fake-jwt-token", null); // Fake token and user

                when(authService.login(loginDto)).thenReturn(userResponse);

                // Act
                var response = authController.login(loginDto);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(userResponse, response.getBody());

                verify(authService, times(1)).login(loginDto);
            }

            @Test
            @DisplayName("Should propagate exception when credentials are invalid.")
            public void shouldThrowExceptionWhenLoginFails() {
                // Arrange
                var loginDto = new LoginDto("wrongUser", "wrongPass");

                when(authService.login(loginDto))
                        .thenThrow(new BadCredentialsException("Invalid credentials."));

                // Act & Assert
                assertThrows(BadCredentialsException.class, () -> authController.login(loginDto));
            }
        }

        @Nested
        @DisplayName("Tests for Me (Current user).")
        public class MeTests {

            @Test
            @DisplayName("Should return 200 OK when user is authenticated.")
            public void shouldReturnUserWhenAuthenticated() {
                // Arrange
                User mockUser = new User();
                UserDto mockDto = new UserDto(
                    "1", 
                    "User", 
                    "user@email.com", 
                    Role.ADMIN, 
                    "http://example.com/avatar.jpg"
                );

                when(authService.getAuthenticatedUserDto(mockUser)).thenReturn(mockDto);

                // Act
                var response = authController.me(mockUser);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(mockDto, response.getBody());
            }

            @Test
            @DisplayName("Should Return 401 UNAUTHORIZED when user is null.")
            public void shouldReturnUnauthorizedWhenUserIsNull() {
                // Arrange & Act
                var response = authController.me(null);

                // Assert
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            }

        }

        @Nested
        @DisplayName("Tests for logout.")
        public class LogoutTests {

            @Test
            @DisplayName("Should return 204 NO CONTENT on logout.")
            public void shouldLogoutWithSuccess() {
                // Arrange
                String token = "Bearer fake-token";
                when(request.getHeader("Authorization")).thenReturn(token);

                // Act
                var response = authController.logout(request);

                // Assert
                assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
                verify(authService, times(1)).logout(token);
            }

        }

    }
}
