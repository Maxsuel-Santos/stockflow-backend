package github.maxsuel.agregadordeinvestimentos.repository;

import github.maxsuel.agregadordeinvestimentos.entity.Account;
import github.maxsuel.agregadordeinvestimentos.entity.BillingAddress;
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
@DisplayName("Tests for BillingAddress Repository.")
public class BillingAddressRepositoryTest {

    @Autowired
    private BillingAddressRepository billingAddressRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    @DisplayName("Tests for Find Methods.")
    public class FindMethodsTests {

        @Test
        @DisplayName("Should find billing address by ID successfully.")
        public void findByIdSuccess() {
            // Arrange
            User user = createAndPersistUser("address_owner", "owner@email.com");
            Account account = createAndPersistAccount(user, "Investments");
            createAndPersistBillingAddress(account, "Olívia Flores", 1000);

            // Act
            // O ID do endereço é o mesmo ID da conta devido ao @MapsId
            Optional<BillingAddress> result = billingAddressRepository.findById(account.getAccountId());

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getStreet()).isEqualTo("Olívia Flores");
            assertThat(result.get().getNumber()).isEqualTo(1000);
            assertThat(result.get().getAccount().getAccountId()).isEqualTo(account.getAccountId());
        }

        @Test
        @DisplayName("Should return empty when address ID does not exist.")
        public void findByIdNotFound() {
            // Act
            Optional<BillingAddress> result = billingAddressRepository.findById(UUID.randomUUID());

            // Assert
            assertThat(result).isEmpty();
        }

    }

    @Nested
    @DisplayName("Tests for Persistence Logic.")
    public class PersistenceTests {

        @Test
        @DisplayName("Should save billing address with shared ID from account.")
        public void saveAddressSuccess() {
            // Arrange
            User user = createAndPersistUser("new_user", "new@email.com");
            Account account = createAndPersistAccount(user, "Savings");

            BillingAddress address = new BillingAddress();
            address.setAccount(account); // O @MapsId cuidará de setar o ID automaticamente
            address.setStreet("Rua das Flores");
            address.setNumber(50);

            // Act
            BillingAddress savedAddress = billingAddressRepository.save(address);
            entityManager.flush();

            // Assert
            assertThat(savedAddress.getId()).isEqualTo(account.getAccountId());
            assertThat(savedAddress.getStreet()).isEqualTo("Rua das Flores");
        }

    }

    // Helper methods
    private User createAndPersistUser(String username, String email) {
        User user = new User(username, email, "password");
        return entityManager.persist(user);
    }

    private Account createAndPersistAccount(User user, String description) {
        Account account = new Account(user, description, new ArrayList<>());
        return entityManager.persist(account);
    }

    private void createAndPersistBillingAddress(Account account, String street, Integer number) {
        BillingAddress address = new BillingAddress();
        address.setAccount(account);
        address.setStreet(street);
        address.setNumber(number);
        entityManager.persist(address);
    }

}
