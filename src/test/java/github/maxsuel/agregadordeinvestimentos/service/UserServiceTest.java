package github.maxsuel.agregadordeinvestimentos.service;

import github.maxsuel.agregadordeinvestimentos.dto.request.account.CreateAccountDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.user.UpdateUserDto;
import github.maxsuel.agregadordeinvestimentos.entity.Account;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.exceptions.UserNotFoundException;
import github.maxsuel.agregadordeinvestimentos.mapper.AccountMapper;
import github.maxsuel.agregadordeinvestimentos.mapper.BillingAddressMapper;
import github.maxsuel.agregadordeinvestimentos.repository.AccountRepository;
import github.maxsuel.agregadordeinvestimentos.repository.BillingAddressRepository;
import github.maxsuel.agregadordeinvestimentos.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private BillingAddressRepository billingAddressRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private BillingAddressMapper billingAddressMapper;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Nested
    @DisplayName("Tests for Updating User.")
    public class UpdateUser {

        @Test
        @DisplayName("Should update user when it exists.")
        public void shouldUpdateUserWhenUserExists() {
            // Arrange
            var userId = UUID.randomUUID();
            var updateDto = new UpdateUserDto("newUsername", "newPassword");
            var existingUser = new User("oldUsername", "old@email.com", "oldPassword");
            existingUser.setUserId(userId);

            doReturn("hashedPassword").when(passwordEncoder).encode(any());

            doReturn(Optional.of(existingUser))
                    .when(userRepository)
                    .findById(userId);

            doReturn(existingUser)
                    .when(userRepository)
                    .save(any(User.class));

            // Act
            userService.updateUserById(userId.toString(), updateDto);

            // Assert
            verify(userRepository).save(userArgumentCaptor.capture());
            var userCaptured = userArgumentCaptor.getValue();

            assertThat(userCaptured.getUsername()).isEqualTo(updateDto.username());
            assertThat(userCaptured.getPassword()).isEqualTo("hashedPassword");
        }

        @Test
        @DisplayName("Should throw exception when user does not exist in update.")
        public void shouldThrowExceptionWhenUserDoesNotExist() {
            // Arrange
            var userId = UUID.randomUUID().toString();
            var updateDto = new UpdateUserDto("username", "password");
            doReturn(Optional.empty()).when(userRepository).findById(any(UUID.class));

            // Act & Assert
            assertThatThrownBy(() -> userService.updateUserById(userId, updateDto))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User not found with ID: " + userId);
        }
    }

    @Nested
    @DisplayName("Tests for Getting User by ID.")
    public class getUserById {

        @Test
        @DisplayName("Should get user by id with success when optional is present.")
        // Arrange
        public void shouldGetUserByIdWithSuccessWhenOptionalIsPresent() {
            var user = new User(
                UUID.randomUUID(),
                "username",
                "email@email.com",
                "password",
                Instant.now(),
                null
            );

            doReturn(Optional.of(user))
                    .when(userRepository)
                    .findById(uuidArgumentCaptor.capture());
            
            // Act
            var output = userService.getUserById(user.getUserId().toString());

            // Assert
            assertTrue(output.isPresent());
            assertEquals(user.getUserId(), uuidArgumentCaptor.getValue());
        }

        @Test
        @DisplayName("Should get user by id with success when optional is empty.")
        // Arrange
        public void shouldGetUserByIdWithSuccessWhenOptionalIsEmpty() {
            var userId = UUID.randomUUID();

            doReturn(Optional.empty())
                    .when(userRepository)
                    .findById(uuidArgumentCaptor.capture());
            
            // Act
            var output = userService.getUserById(userId.toString());

            // Assert
            assertTrue(output.isEmpty());
            assertEquals(userId, uuidArgumentCaptor.getValue());
        }
    }

    @Nested
    @DisplayName("Tests for Listing Users")
    public class listUsers {

        @Test
        @DisplayName("Should return all users with success.")
        public void shouldReturnAllUsersWithSuccess() {
            // Arrange
            var user = new User(
                UUID.randomUUID(),
                "username",
                "email@email.com",
                "password",
                Instant.now(),
                null
            );

            var userList = List.of(user);

            doReturn(userList)
                    .when(userRepository)
                    .findAll();

            // Act
            var output = userService.listAllUsers();

            // Assert
            assertNotNull(output);
            assertEquals(userList.size(), output.size());
        }

    }

    @Nested
    @DisplayName("Tests for Deleting User.")
    public class DeleteUserById {

        @Test
        @DisplayName("Should delete user with success when it exists.")
        public void shouldDeleteUserWithSuccessWhenItExists() {
            // Arrange
            doReturn(true)
                    .when(userRepository)
                    .existsById(uuidArgumentCaptor.capture());

            doNothing()
                    .when(userRepository)
                    .deleteById(uuidArgumentCaptor.capture());
            
            var userId = UUID.randomUUID();

            // Act
            userService.deleteUser(userId.toString());

            // Assert
            var idList = uuidArgumentCaptor.getAllValues();

            assertEquals(userId, idList.get(0));
            assertEquals(userId, idList.get(1));

            verify(userRepository, times(1)).existsById(idList.get(0));
            verify(userRepository, times(1)).deleteById(idList.get(1));
        }

        @Test
        @DisplayName("Should not delete user when it does not exist.")
        public void shouldNotDeleteUserWhenItDoesNotExist() {
            // Arrange
            doReturn(false)
                    .when(userRepository)
                    .existsById(uuidArgumentCaptor.capture());
            
            var userId = UUID.randomUUID();

            // Act
            userService.deleteUser(userId.toString());

            // Assert
            assertEquals(userId, uuidArgumentCaptor.getValue());

            verify(userRepository, times(1)).existsById(uuidArgumentCaptor.getValue());
            verify(userRepository, times(0)).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Tests for Updating User by ID.")
    public class updateUserById {

        @Test
        @DisplayName("Should update user by id when it exists and username and password are filled.")
        public void shouldUpdateUserByIdWhenItExistsAndUsernameAndPasswordAreFilled() {
            // Arrange
            var updateUserDto = new UpdateUserDto("newUsername", "newPassword");
            var encodedPassword = "hashedPassword123";
            var userId = UUID.randomUUID();
            var user = new User(userId, "username", "email@email.com", "password", Instant.now(), null);

            doReturn(encodedPassword).when(passwordEncoder).encode(updateUserDto.password());

            doReturn(Optional.of(user))
                    .when(userRepository)
                    .findById(uuidArgumentCaptor.capture());

            doReturn(user)
                    .when(userRepository)
                    .save(userArgumentCaptor.capture());

            // Act
            userService.updateUserById(user.getUserId().toString(), updateUserDto);

            // Assert
            var userCaptured = userArgumentCaptor.getValue();

            assertEquals(updateUserDto.username(), userCaptured.getUsername());
            assertEquals(encodedPassword, userCaptured.getPassword());

            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should not update user by id when it does not exists.")
        public void shouldNotUpdateUserByIdWhenItDoesNotExists() {
            // Arrange
            var updateUserDto = new UpdateUserDto(
                "newUsername", 
                "newPassword"
            );

            var userId = UUID.randomUUID();

            doReturn(Optional.empty())
                    .when(userRepository)
                    .findById(uuidArgumentCaptor.capture());

            // Act
            assertThatThrownBy(() -> userService.updateUserById(userId.toString(), updateUserDto))
                    .isInstanceOf(UserNotFoundException.class);

            // Assert
            assertEquals(userId, uuidArgumentCaptor.getValue());

            verify(userRepository, times(1)).findById(uuidArgumentCaptor.getValue());
            verify(userRepository, times(0)).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for Account Management.")
    public class AccountManagementTests {

        @Test
        @DisplayName("Should create account and billing address successfully.")
        public void createAccount_Success() {
            // Arrange
            var userId = UUID.randomUUID();
            var user = new User();
            user.setUserId(userId);
            var dto = new CreateAccountDto("Main Wallet", "Street 123", 100);
            var account = new Account();
            account.setAccountId(UUID.randomUUID());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(accountMapper.toEntity(dto, user)).thenReturn(account);
            when(accountRepository.save(account)).thenReturn(account);

            // Act
            userService.createAccount(userId.toString(), dto);

            // Assert
            verify(accountRepository).save(account);
            verify(billingAddressRepository).save(any());
            verify(billingAddressMapper).toEntity(eq(dto), eq(account));
        }

        @Test
        @DisplayName("Should list all accounts for a user.")
        public void listAllAccounts_Success() {
            // Arrange
            var userId = UUID.randomUUID();
            var user = new User();
            user.setUserId(userId);
            var account = new Account();
            account.setAccountId(UUID.randomUUID());
            user.setAccounts(List.of(account));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(accountService.listAllStocks(anyString())).thenReturn(new ArrayList<>());

            // Act
            var result = userService.listAllAccounts(userId.toString());

            // Assert
            assertNotNull(result);
            verify(accountMapper, times(1)).toDto(any(), any());
        }
    }

    @Nested
    @DisplayName("Tests for Updating User.")
    public class UpdateUserTests {

        @Test
        @DisplayName("Should update only username when password is null.")
        public void shouldUpdateOnlyUsername() {
            var userId = UUID.randomUUID();
            var updateDto = new UpdateUserDto("newUsername", null);
            var existingUser = new User("old", "old@email.com", "pass");

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

            userService.updateUserById(userId.toString(), updateDto);

            verify(userRepository).save(userArgumentCaptor.capture());
            assertEquals("newUsername", userArgumentCaptor.getValue().getUsername());
            verify(passwordEncoder, never()).encode(any());
        }
    }

}
