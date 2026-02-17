package github.maxsuel.agregadordeinvestimentos.controller;

import github.maxsuel.agregadordeinvestimentos.dto.request.account.CreateAccountDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.user.UpdateUserDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountResponseDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountStockResponseDto;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.exceptions.UserNotFoundException;
import github.maxsuel.agregadordeinvestimentos.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Nested
    @DisplayName("Tests for Get User By ID")
    public class GetUserById {

        @Test
        @DisplayName("Should return 200 OK when user exists")
        public void shouldReturn200OkWhenUserExists() {
            // Arrange
            var userId = UUID.randomUUID().toString();
            var user = new User("username", "username@email.com", "123");
            when(userService.getUserById(userId)).thenReturn(Optional.of(user));

            // Act
            var response = userController.getUserById(userId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(user, response.getBody());
        }

        @Test
        @DisplayName("Should return 404 Not Found when user does not exist")
        public void shouldReturn404NotFound() {
            // Arrange
            var userId = UUID.randomUUID().toString();
            when(userService.getUserById(userId)).thenReturn(Optional.empty());

            // Act
            var response = userController.getUserById(userId);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("Tests for List All Users")
    public class ListUsers {

        @Test
        @DisplayName("Should return 200 OK with list of users")
        public void shouldReturnListWithSuccess() {
            // Arrange
            var user = new User("username", "username@email.com", "123");
            when(userService.listAllUsers()).thenReturn(List.of(user));

            // Act
            var response = userController.listAllUsers();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assert response.getBody() != null;
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("Should return 200 OK even if list is empty")
        public void shouldReturnEmptyListSuccess() {
            // Arrange
            when(userService.listAllUsers()).thenReturn(Collections.emptyList());

            // Act
            var response = userController.listAllUsers();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assert response.getBody() != null;
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("Tests for Update User")
    public class UpdateUser {

        @Test
        @DisplayName("Should return 204 No Content on success")
        public void shouldUpdateWithSuccess() {
            // Arrange
            var userId = UUID.randomUUID().toString();
            var dto = new UpdateUserDto("newUsername", "321");
            doNothing().when(userService).updateUserById(userId, dto);

            // Act
            var response = userController.updateUserById(userId, dto);

            // Assert
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        @DisplayName("Should propagate UserNotFoundException when user missing")
        public void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            var userId = "invalid-id";
            var dto = new UpdateUserDto("name", "pass");
            
            doThrow(new UserNotFoundException("Not found"))
                .when(userService).updateUserById(userId, dto);

            // Act & Assert
            assertThrows(UserNotFoundException.class, () -> userController.updateUserById(userId, dto));
        }
    }

    @Nested
    @DisplayName("Tests for Delete User")
    public class DeleteUser {

        @Test
        @DisplayName("Should return 204 No Content on success")
        public void shouldDeleteWithSuccess() {
            // Arrange
            var userId = UUID.randomUUID().toString();
            doNothing().when(userService).deleteUser(userId);

            // Act
            var response = userController.deleteUser(userId);

            // Assert
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(userService, times(1)).deleteUser(userId);
        }

        @Test
        @DisplayName("Should handle service failure on delete")
        public void shouldHandleDeleteFailure() {
            // Arrange
            var userId = UUID.randomUUID().toString();
            doThrow(new RuntimeException("Database down"))
                .when(userService).deleteUser(userId);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                userController.deleteUser(userId);
            });
        }
    }

    @Nested
    @DisplayName("Tests for Account Management")
    public class AccountManagement {

        @Test
        @DisplayName("Should return 200 OK when account is created successfully")
        public void shouldReturn200OkOnCreateAccount() {
            // Arrange
            var userId = UUID.randomUUID().toString();
            var dto = new CreateAccountDto(
                    "Carteira de dividendos",
                    "Olívia Flores",
                    1500
            );

            doNothing().when(userService).createAccount(userId, dto);

            // Act
            var response = userController.createAccount(userId, dto);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(userService, times(1)).createAccount(userId, dto);
        }

        @Test
        @DisplayName("Should return 200 OK and list of accounts with detailed stock info")
        public void shouldReturnAccountsWithDetailedStocksSuccess() {
            // Arrange
            var userId = UUID.randomUUID().toString();

            var detailedStockDto = new AccountStockResponseDto(
                    "ITUB4",
                    "Itaú Unibanco",
                    "Banco Itaú Unibanco PN",
                    100,
                    32.45,
                    3245.00,
                    "https://icons.brapi.dev/icons/ITUB4.svg"
            );

            var accountDto = new AccountResponseDto(
                    UUID.randomUUID().toString(),
                    "Carteira de Longo Prazo",
                    List.of(detailedStockDto)
            );

            when(userService.listAllAccounts(userId)).thenReturn(List.of(accountDto));

            // Act
            var response = userController.listAllAccounts(userId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            var stockResult = response.getBody().getFirst().stocks().getFirst();
            assertEquals("ITUB4", stockResult.stockId());
            assertEquals("Itaú Unibanco", stockResult.name());
            assertEquals(3245.00, stockResult.total());
            assertNotNull(stockResult.logoUrl());
        }
    }

}
