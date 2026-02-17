package github.maxsuel.agregadordeinvestimentos.repository;

import github.maxsuel.agregadordeinvestimentos.entity.Account;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests for Account Repository.")
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    @DisplayName("Tests for Find Methods.")
    public class FindMethodsTests {

        @Test
        @DisplayName("Should find account by ID successfully.")
        public void findByIdSuccess() {
            // Arrange
            User user = createAndPersistUser("account_owner", "owner@test.com");
            Account account = createAndPersistAccount(user, "Main Wallet");

            // Act
            Optional<Account> result = accountRepository.findById(account.getAccountId());

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getDescription()).isEqualTo("Main Wallet");
            assertThat(result.get().getUser().getUsername()).isEqualTo("account_owner");
        }

        @Test
        @DisplayName("Should return empty when account ID does not exist.")
        public void findByIdNotFound() {
            // Act
            Optional<Account> result = accountRepository.findById(UUID.randomUUID());

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests for Persistence Logic.")
    public class PersistenceTests {

        @Test
        @DisplayName("Should save account and link to user correctly.")
        public void saveAccountSuccess() {
            // Arrange
            User user = createAndPersistUser("persistence_user", "persist@test.com");
            Account account = new Account(user, "New Account", new ArrayList<>());

            // Act
            Account savedAccount = accountRepository.save(account);
            entityManager.flush();

            // Assert
            assertThat(savedAccount.getAccountId()).isNotNull();
            assertThat(savedAccount.getDescription()).isEqualTo("New Account");
            assertThat(savedAccount.getUser().getUserId()).isEqualTo(user.getUserId());
        }

        @Test
        @DisplayName("Should update account description successfully.")
        public void updateAccountDescriptionSuccess() {
            // Arrange
            User user = createAndPersistUser("update_user", "update@test.com");
            Account account = createAndPersistAccount(user, "Old Name");

            // Act
            account.setDescription("Updated Name");
            accountRepository.save(account);
            entityManager.flush();

            // Assert
            Account updatedAccount = entityManager.find(Account.class, account.getAccountId());
            assert updatedAccount != null;
            assertThat(updatedAccount.getDescription()).isEqualTo("Updated Name");
        }
    }

    // Helper methods
    private User createAndPersistUser(String username, String email) {
        User user = new User(username, email, "encoded_password");
        return entityManager.persist(user);
    }

    private Account createAndPersistAccount(User user, String description) {
        Account account = new Account(user, description, new ArrayList<>());
        return entityManager.persist(account);
    }

}
