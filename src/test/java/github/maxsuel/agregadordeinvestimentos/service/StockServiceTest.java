package github.maxsuel.agregadordeinvestimentos.service;

import github.maxsuel.agregadordeinvestimentos.client.BrapiClient;
import github.maxsuel.agregadordeinvestimentos.dto.external.brapi.BrapiResponseDto;
import github.maxsuel.agregadordeinvestimentos.dto.external.brapi.StockDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.stock.CreateStockDto;
import github.maxsuel.agregadordeinvestimentos.entity.Account;
import github.maxsuel.agregadordeinvestimentos.entity.AccountStock;
import github.maxsuel.agregadordeinvestimentos.entity.Stock;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.mapper.StockMapper;
import github.maxsuel.agregadordeinvestimentos.repository.StockRepository;
import github.maxsuel.agregadordeinvestimentos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockMapper stockMapper;

    @Mock
    private BrapiClient brapiClient;

    @InjectMocks
    private StockService stockService;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(stockService, "TOKEN", "test-token");
    }

    @Nested
    @DisplayName("Stock Creation.")
    public class CreateStockTests {

        @Test
        @DisplayName("Should call repository to save stock.")
        public void createStock_Success() {
            // Arrange
            var dto = new CreateStockDto("VALE3", "Vale S.A.");
            var stock = new Stock("VALE3", "Vale", "Vale S.A.", "Mining", "url", "Desc");

            when(stockMapper.toEntity(dto)).thenReturn(stock);

            // Act
            stockService.createStock(dto);

            // Assert
            verify(stockRepository, times(1)).save(stock);
        }

    }

    @Nested
    @DisplayName("Stock Listing and Aggregation.")
    public class ListOwnedStocksTests {

        @Test
        @DisplayName("Should aggregate stocks from different accounts correctly.")
        public void listOwnedStocks_AggregationSuccess() {
            // Arrange
            var userId = UUID.randomUUID();
            var user = new User();
            user.setUserId(userId);

            var acc1 = new Account();
            var acc2 = new Account();

            var stock = new Stock("ITUB4", "Itau", "Itau Unibanco", "Finance", "https://logo.url/ITUB4.svg", "Bank");

            var as1 = new AccountStock(null, acc1, stock, 10, BigDecimal.valueOf(30.00));
            var as2 = new AccountStock(null, acc2, stock, 15, BigDecimal.valueOf(40.00));

            acc1.setAccountStocks(List.of(as1));
            acc2.setAccountStocks(List.of(as2));
            user.setAccounts(List.of(acc1, acc2));

            var brapiStock = new StockDto(
                    "ITUB4",
                    "Itau",
                    "Itau Unibanco",
                    35.0,
                    0.5,
                    100L,
                    "BRL",
                    "https://logo.url/ITUB4.svg"
            );
            var brapiResponse = new BrapiResponseDto(List.of(brapiStock));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(brapiClient.getQuote(anyString(), anyString())).thenReturn(brapiResponse);

            // Act
            var result = stockService.listOwnedStocks(user);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            var dto = result.getFirst();
            assertEquals("ITUB4", dto.stockId());
            assertEquals(25, dto.quantity());
            assertEquals(36.00, dto.avgPrice(), 0.001);

            assertNotNull(dto.logoUrl(), "Logo URL should not be null");
            assertTrue(dto.logoUrl().contains("ITUB4"));
        }

        @Test
        @DisplayName("Should throw exception when user is not found.")
        public void listOwnedStocks_UserNotFound() {
            // Arrange
            var user = new User();
            var userId = UUID.randomUUID();
            user.setUserId(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RuntimeException.class, () -> stockService.listOwnedStocks(user));
        }

    }

}
