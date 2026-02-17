package github.maxsuel.agregadordeinvestimentos.repository;

import github.maxsuel.agregadordeinvestimentos.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests for AccountStock Repository.")
public class AccountStockRepositoryTest {

    @Autowired
    private AccountStockRepository accountStockRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    @DisplayName("Tests for Find Methods.")
    public class FindMethodsTests {

        @Test
        @DisplayName("Should find account stock by composite ID successfully.")
        public void findByIdSuccess() {
            // Arrange
            User user = createAndPersistUser("investor", "investor@test.com");
            Account account = createAndPersistAccount(user, "Investment Wallet");
            Stock stock = createAndPersistStock("PETR4", "Petrobras");

            AccountStockId id = new AccountStockId(account.getAccountId(), stock.getStockId());
            createAndPersistAccountStock(id, account, stock, 100, new BigDecimal("35.50"));

            // Act
            Optional<AccountStock> result = accountStockRepository.findById(id);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getQuantity()).isEqualTo(100);
            assertThat(result.get().getAveragePrice()).isEqualByComparingTo("35.50");
            assertThat(result.get().getStock().getStockId()).isEqualTo("PETR4");
        }

        @Test
        @DisplayName("Should return empty when composite ID does not exist.")
        public void findByIdNotFound() {
            // Act
            AccountStockId id = new AccountStockId(java.util.UUID.randomUUID(), "GHOST4");
            Optional<AccountStock> result = accountStockRepository.findById(id);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests for Persistence Logic.")
    public class PersistenceTests {

        @Test
        @DisplayName("Should save account stock and update average price correctly.")
        public void saveAccountStockSuccess() {
            // Arrange
            User user = createAndPersistUser("trader", "trader@test.com");
            Account account = createAndPersistAccount(user, "Trading Wallet");
            Stock stock = createAndPersistStock("VALE3", "Vale S.A.");

            AccountStockId id = new AccountStockId(account.getAccountId(), stock.getStockId());
            AccountStock accountStock = new AccountStock(id, account, stock, 50, new BigDecimal("110.00"));

            // Act
            AccountStock saved = accountStockRepository.save(accountStock);
            entityManager.flush();

            // Assert
            assertThat(saved.getId()).isEqualTo(id);
            assertThat(saved.getQuantity()).isEqualTo(50);
            assertThat(saved.getAveragePrice()).isEqualByComparingTo("110.00");
        }
    }

    // Helper methods
    private User createAndPersistUser(String username, String email) {
        User user = new User(username, email, "pass");
        return entityManager.persist(user);
    }

    private Account createAndPersistAccount(User user, String description) {
        Account account = new Account(user, description, new ArrayList<>());
        return entityManager.persist(account);
    }

    private Stock createAndPersistStock(String ticker, String name) {
        Stock stock = new Stock(ticker, name);
        return entityManager.persist(stock);
    }

    private void createAndPersistAccountStock(AccountStockId id, Account account, Stock stock, Integer qty, BigDecimal price) {
        AccountStock accountStock = new AccountStock(id, account, stock, qty, price);
        entityManager.persist(accountStock);
    }
}