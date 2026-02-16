package github.maxsuel.agregadordeinvestimentos.service;

import github.maxsuel.agregadordeinvestimentos.client.BrapiClient;
import github.maxsuel.agregadordeinvestimentos.dto.external.brapi.BrapiResponseDto;
import github.maxsuel.agregadordeinvestimentos.dto.external.brapi.StockDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.stock.TradeRequestDto;
import github.maxsuel.agregadordeinvestimentos.entity.*;
import github.maxsuel.agregadordeinvestimentos.entity.enums.TradeType;
import github.maxsuel.agregadordeinvestimentos.exceptions.InsufficientFundsException;
import github.maxsuel.agregadordeinvestimentos.exceptions.InsufficientSharesException;
import github.maxsuel.agregadordeinvestimentos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

    @Mock
    private BrapiClient brapiClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountStockRepository accountStockRepository;

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private TradeService tradeService;

    @Captor
    private ArgumentCaptor<AccountStock> accountStockArgumentCaptor;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Captor
    private ArgumentCaptor<Transactions> transactionsArgumentCaptor;

    private User user;
    private final UUID accountId = UUID.randomUUID();
    private final String stockId = "VALE3";

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(tradeService, "TOKEN", "test-token");
        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setCash(new BigDecimal("1000.00"));
    }

    @Nested
    @DisplayName("Buy Operations.")
    public class BuyTests {

        @Test
        @DisplayName("Should execute buy and update average price correctly.")
        public void executeBuy_Success() {
            // Arrange
            var dto = new TradeRequestDto(stockId, 10, accountId);
            var brapiResponse = new BrapiResponseDto((List.of(new StockDto(stockId, "", "", 50.0, "BRL", ""))));

            var account = new Account();
            var stock = new Stock();

            var existingStock = new AccountStock(new AccountStockId(accountId, stockId), account, stock, 10, new BigDecimal("30.00"));

            when(brapiClient.getQuote(anyString(), anyString())).thenReturn(brapiResponse);
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
            when(accountStockRepository.findById(any())).thenReturn(Optional.of(existingStock));

            // Act
            tradeService.executeBuy(user, dto);

            // Assert
            verify(accountStockRepository).save(accountStockArgumentCaptor.capture());
            verify(userRepository).save(userArgumentCaptor.capture());
            verify(transactionsRepository).save(transactionsArgumentCaptor.capture());

            AccountStock savedStock = accountStockArgumentCaptor.getValue();

            assertEquals(20, savedStock.getQuantity());
            assertEquals(0, new BigDecimal("40.0000").compareTo(savedStock.getAveragePrice()));
            assertEquals(0, new BigDecimal("500.00").compareTo(userArgumentCaptor.getValue().getCash())); // 1000 - 500
            assertEquals(TradeType.BUY, transactionsArgumentCaptor.getValue().getType());

        }

        @Test
        @DisplayName("Should throw exception when user has no money.")
        void executeBuy_InsufficientFunds() {
            // Arrange & Act
            var dto = new TradeRequestDto(stockId, 1000, accountId);
            var brapiResponse = new BrapiResponseDto(List.of(new StockDto(stockId, "", "", 50.0, "BRL", "")));

            when(brapiClient.getQuote(anyString(), anyString())).thenReturn(brapiResponse);

            // Assert
            assertThrows(InsufficientFundsException.class, () -> tradeService.executeBuy(user, dto));
            verify(userRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("Sell Operations.")
    class SellTests {

        @Test
        @DisplayName("Should execute sell and update user cash.")
        void executeSell_Success() {
            // Arrange
            var dto = new TradeRequestDto(stockId, 5, accountId);
            var brapiResponse = new BrapiResponseDto(List.of(new StockDto(stockId, "", "", 60.0, "BRL", "")));
            var existingStock = new AccountStock(null, new Account(), new Stock(), 10, new BigDecimal("30.00"));

            when(accountStockRepository.findById(any())).thenReturn(Optional.of(existingStock));
            when(brapiClient.getQuote(anyString(), anyString())).thenReturn(brapiResponse);

            // Act
            tradeService.executeSell(user, dto);

            // Assert
            verify(userRepository).save(userArgumentCaptor.capture());
            assertEquals(0, new BigDecimal("1300.00").compareTo(userArgumentCaptor.getValue().getCash()));
            assertEquals(5, existingStock.getQuantity());
        }

        @Test
        @DisplayName("Should delete stock record when quantity reaches zero.")
        void executeSell_CompleteLiquidation() {
            var dto = new TradeRequestDto(stockId, 10, accountId);
            var brapiResponse = new BrapiResponseDto(List.of(new StockDto(stockId, "", "", 60.0, "BRL", "")));
            var existingStock = new AccountStock(null, new Account(), new Stock(), 10, new BigDecimal("30.00"));

            when(accountStockRepository.findById(any())).thenReturn(Optional.of(existingStock));
            when(brapiClient.getQuote(anyString(), anyString())).thenReturn(brapiResponse);

            tradeService.executeSell(user, dto);

            verify(accountStockRepository).delete(existingStock);
        }

        @Test
        @DisplayName("Should throw exception when selling more than owned.")
        void executeSell_InsufficientShares() {
            var dto = new TradeRequestDto(stockId, 50, accountId);
            var existingStock = new AccountStock(null, new Account(), new Stock(), 10, new BigDecimal("30.00"));

            when(accountStockRepository.findById(any())).thenReturn(Optional.of(existingStock));

            assertThrows(InsufficientSharesException.class, () -> tradeService.executeSell(user, dto));
        }
    }

}
