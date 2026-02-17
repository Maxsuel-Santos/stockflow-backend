package github.maxsuel.agregadordeinvestimentos.mapper;

import github.maxsuel.agregadordeinvestimentos.dto.external.brapi.StockDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountStockResponseDto;
import github.maxsuel.agregadordeinvestimentos.entity.AccountStock;
import github.maxsuel.agregadordeinvestimentos.entity.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests for AccountStock Mapper.")
public class AccountStockMapperTest {

    private final AccountStockMapper mapper = Mappers.getMapper(AccountStockMapper.class);

    @Nested
    @DisplayName("Tests for Mapping to DTO.")
    public class ToDtoMappingTests {

        @Test
        @DisplayName("Should map AccountStock and StockDto to AccountStockResponseDto successfully.")
        public void shouldMapToDtoSuccess() {
            // Arrange
            var accountStock = new AccountStock();
            accountStock.setQuantity(2);
            accountStock.setStock(new Stock("PETR4", "Petrobras"));

            var stockDto = new StockDto(
                    "PETR4",
                    "Petrobras PN",
                    "Petroleo Brasileiro SA",
                    30.00,
                    "BRL",
                    "https://logo.url"
            );

            // Act
            AccountStockResponseDto result = mapper.toDto(accountStock, stockDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.stockId()).isEqualTo("PETR4");
            assertThat(result.price()).isEqualTo(30.00);
            assertThat(result.total()).isEqualTo(60.00);
        }

    }

}
