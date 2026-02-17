package github.maxsuel.agregadordeinvestimentos.repository;

import github.maxsuel.agregadordeinvestimentos.entity.Stock;
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
@DisplayName("Tests for Stock Repository.")
public class StockRepositoryTest {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    @DisplayName("Tests for Find Methods.")
    public class FindMethodsTests {

        @Test
        @DisplayName("Should find stock by ID (Ticker) successfully.")
        public void findByIdSuccess() {
            // Arrange
            Stock stock = createAndPersistStock("PETR4", "Petróleo Brasileiro S.A.");

            // Act
            Optional<Stock> result = stockRepository.findById("PETR4");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getDescription()).isEqualTo("Petróleo Brasileiro S.A.");
        }

        @Test
        @DisplayName("Should return empty when stock ticker does not exist.")
        public void findByIdNotFound() {
            // Act
            Optional<Stock> result = stockRepository.findById("AAPL34");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests for Persistence Logic.")
    public class PersistenceTests {

        @Test
        @DisplayName("Should save stock with manual ID successfully.")
        public void saveStockSuccess() {
            // Arrange
            Stock stock = new Stock("VALE3", "Vale S.A.");

            // Act
            Stock savedStock = stockRepository.save(stock);
            entityManager.flush();

            // Assert
            assertThat(savedStock.getStockId()).isEqualTo("VALE3");
            assertThat(entityManager.find(Stock.class, "VALE3")).isNotNull();
        }

        @Test
        @DisplayName("Should update stock description successfully.")
        public void updateStockDescriptionSuccess() {
            // Arrange
            Stock stock = createAndPersistStock("ITUB4", "Itaú Unibanco");

            // Act
            stock.setDescription("Itaú Unibanco Holding S.A.");
            stockRepository.save(stock);
            entityManager.flush();

            // Assert
            Stock updated = entityManager.find(Stock.class, "ITUB4");
            assert updated != null;
            assertThat(updated.getDescription()).isEqualTo("Itaú Unibanco Holding S.A.");
        }
    }

    // Helper method
    private Stock createAndPersistStock(String stockId, String description) {
        Stock stock = new Stock(stockId, description);
        return entityManager.persist(stock);
    }

}
