package github.maxsuel.agregadordeinvestimentos.mapper;

import github.maxsuel.agregadordeinvestimentos.dto.response.stock.TransactionsResponseDto;
import github.maxsuel.agregadordeinvestimentos.entity.Stock;
import github.maxsuel.agregadordeinvestimentos.entity.Transactions;
import github.maxsuel.agregadordeinvestimentos.entity.enums.TradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests for Transaction Mapper.")
public class TransactionMapperTest {

    private final TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

    @Nested
    @DisplayName("Tests for Mapping to DTO.")
    public class ToDtoMappingTests {

        @Test
        @DisplayName("Should map Transactions entity to TransactionsResponseDto with calculated total value.")
        public void shouldMapToDtoSuccess() {
            // Arrange
            var stock = new Stock("ITUB4", "Itaú Unibanco");
            var price = new BigDecimal("30.00");
            var quantity = 10;
            var expectedTotal = new BigDecimal("300.00");

            Transactions transaction = Transactions.builder()
                    .transactionId(UUID.randomUUID())
                    .stock(stock)
                    .priceAtTime(price)
                    .quantity(quantity)
                    .type(TradeType.BUY)
                    .timestamp(Instant.now())
                    .build();

            // Act
            TransactionsResponseDto responseDto = mapper.toDto(transaction);

            // Assert
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.stockId()).isEqualTo("ITUB4");
            assertThat(responseDto.quantity()).isEqualTo(10);

            // Expression
            assertThat(responseDto.totalValue())
                    .withFailMessage("The total value should be the product of price and quantity.")
                    .isEqualByComparingTo(expectedTotal);

            assertThat(responseDto.type()).isEqualTo(TradeType.BUY);
        }

    }

}
