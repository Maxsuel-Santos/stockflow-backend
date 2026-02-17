package github.maxsuel.agregadordeinvestimentos.repository;

import github.maxsuel.agregadordeinvestimentos.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests for User Repository.")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    @DisplayName("Tests for Find Methods.")
    public class FindMethodsTests {

        @Test
        @DisplayName("Should find user by ID successfully.")
        public void findByIdSuccess() {
            // Arrange & Act
            User user = createAndPersistUser("john_doe", "john@email.com");

            Optional<User> result = userRepository.findById(user.getUserId());

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("john_doe");
        }

        @Test
        @DisplayName("Should find user by username.")
        public void findByUsernameSuccess() {
            // Arrange & Act
            createAndPersistUser("user_name", "username@email.com");

            Optional<User> result = userRepository.findByUsername("user_name");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("username@email.com");
        }

        @Test
        @DisplayName("Should return empty when username does not exist.")
        public void findByUsernameNotFound() {
            // Arrange, Act & Assert
            Optional<User> result = userRepository.findByUsername("ghost_user");
            assertThat(result).isEmpty();
        }

    }

    @Nested
    @DisplayName("Tests for Exists Methods.")
    public class ExistsMethodsTests {

        @Test
        @DisplayName("Should return true when username exists.")
        public void existsByUsernameTrue() {
            // Arrange & Act
            createAndPersistUser("existing_user", "exists@email.com");

            boolean exists = userRepository.existsByUsername("existing_user");

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return true when email exists.")
        public void existsByEmailTrue() {
            // Arrange & Act
            createAndPersistUser("user1", "unique@email.com");

            boolean exists = userRepository.existsByEmail("unique@email.com");

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when data does not exist.")
        public void existsDataFalse() {
            // Assert
            assertThat(userRepository.existsByUsername("not_found")).isFalse();
            assertThat(userRepository.existsByEmail("not_found@email.com")).isFalse();
        }

    }

    @Nested
    @DisplayName("Tests for Persistence Logic.")
    public class PersistenceTests {

        @Test
        @DisplayName("Should save user and populate automatic fields.")
        public void saveUserSuccess() {
            // Arrange & Act
            User user = new User("new_user", "new@email.com", "password");

            User savedUser = userRepository.save(user);
            entityManager.flush();

            // Assert
            assertThat(savedUser.getUserId()).isNotNull();
            assertThat(savedUser.getCreationTimestamp()).isNotNull();
        }

    }

    // Helper method
    private User createAndPersistUser(String username, String email) {
        User user = new User(username, email, "encoded_password");
        return entityManager.persist(user);
    }

}
