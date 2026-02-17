package github.maxsuel.agregadordeinvestimentos.service;

import github.maxsuel.agregadordeinvestimentos.dto.request.stock.CreateStockDto;
import github.maxsuel.agregadordeinvestimentos.entity.Account;
import github.maxsuel.agregadordeinvestimentos.entity.AccountStock;
import github.maxsuel.agregadordeinvestimentos.entity.Stock;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.exceptions.UserNotFoundException;
import github.maxsuel.agregadordeinvestimentos.mapper.StockMapper;
import github.maxsuel.agregadordeinvestimentos.repository.StockRepository;
import github.maxsuel.agregadordeinvestimentos.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockMapper stockMapper;

    @InjectMocks
    private StockService stockService;

    @Nested
    @DisplayName("Stock Creation.")
    public class CreateStockTests {

        @Test
        @DisplayName("Should call repository to save stock.")
        public void createStock_Success() {
            // Arrange
            var dto = new CreateStockDto("VALE3", "Weg S.A.");
            var stock = new Stock();

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

            var stock = new Stock();
            stock.setStockId("ITUB4");

            var as1 = new AccountStock(null, acc1, stock, 10, null);
            var as2 = new AccountStock(null, acc2, stock, 15, null);

            acc1.setAccountStocks(List.of(as1));
            acc2.setAccountStocks(List.of(as2));
            user.setAccounts(List.of(acc1, acc2));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // Act
            var result = stockService.listOwnedStocks(user);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("ITUB4", result.getFirst().stockId());
            assertEquals(25, result.getFirst().totalQuantity()); // 10 + 15
        }

        @Test
        @DisplayName("Should return empty list when user has no stocks.")
        public void listOwnedStocks_Empty() {
            // Arrange
            var user = new User();
            user.setUserId(UUID.randomUUID());
            user.setAccounts(new ArrayList<>());

            when(userRepository.findById(any())).thenReturn(Optional.of(user));

            // Act
            var result = stockService.listOwnedStocks(user);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when user is not found.")
        public void listOwnedStocks_UserNotFound() {
            // Arrange & Act
            var user = new User();
            user.setUserId(UUID.randomUUID());

            when(userRepository.findById(any())).thenReturn(Optional.empty());

            // Assert
            assertThrows(UserNotFoundException.class, () -> stockService.listOwnedStocks(user));
        }

    }

}
