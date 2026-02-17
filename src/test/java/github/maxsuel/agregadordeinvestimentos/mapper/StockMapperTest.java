package github.maxsuel.agregadordeinvestimentos.mapper;

import github.maxsuel.agregadordeinvestimentos.dto.request.stock.CreateStockDto;
import github.maxsuel.agregadordeinvestimentos.entity.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests for Stock Mapper.")
public class StockMapperTest {

    private final StockMapper mapper = Mappers.getMapper(StockMapper.class);

    @Nested
    @DisplayName("Tests for Mapping to Entity.")
    public class ToEntityMappingTests {

        @Test
        @DisplayName("Should map CreateStockDto to Stock Entity successfully.")
        public void shouldMapToEntitySuccess() {
            // Arrange
            var stockId = "ITUB4";
            var description = "Itaú Unibanco Holding S.A.";
            var dto = new CreateStockDto(stockId, description);

            // Act
            Stock result = mapper.toEntity(dto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getStockId())
                    .withFailMessage("The stock ID mapping failed.")
                    .isEqualTo(stockId);

            assertThat(result.getDescription())
                    .withFailMessage("The description mapping failed.")
                    .isEqualTo(description);
        }

    }

}
