package github.maxsuel.agregadordeinvestimentos.controller;

import github.maxsuel.agregadordeinvestimentos.dto.request.stock.CreateStockDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.stock.UserStockSummaryDto;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockControllerTest {

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockController stockController;

    @Nested
    @DisplayName("Tests for Stock Catalog Management.")
    public class CatalogTests {

        @Test
        @DisplayName("Should return 200 OK when is created successfully.")
        public void shouldCreateStockWithSuccess() {
            // Arrange
            var dto = new CreateStockDto("WEGE3", "Weg S.A.");

            doNothing().when(stockService).createStock(dto);

            // Act
            var response = stockController.createStock(dto);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());

            verify(stockService, times(1)).createStock(dto);
        }

    }

    @Nested
    @DisplayName("Tests for User Portfolio Insights.")
    class PortfolioInsightsTests {

        @Test
        @DisplayName("Should return 200 OK and consolidated stock list for user.")
        void shouldReturnOwnedStocksWithSuccess() {
            // Arrange
            var mockUser = new User();
            var summaryList = List.of(
                    new UserStockSummaryDto("ITUB4", 150),
                    new UserStockSummaryDto("PETR4", 50)
            );

            when(stockService.listOwnedStocks(mockUser)).thenReturn(summaryList);

            // Act
            var response = stockController.getOwnedStocks(mockUser);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            assertEquals("ITUB4", response.getBody().getFirst().stockId());
            assertEquals(150, response.getBody().getFirst().totalQuantity());

            verify(stockService, times(1)).listOwnedStocks(mockUser);
        }
    }

}
