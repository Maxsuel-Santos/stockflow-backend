package github.maxsuel.agregadordeinvestimentos.service;

import github.maxsuel.agregadordeinvestimentos.client.BrapiClient;
import github.maxsuel.agregadordeinvestimentos.dto.external.brapi.BrapiResponseDto;
import github.maxsuel.agregadordeinvestimentos.dto.external.brapi.StockDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.account.AssociateAccountStockDto;
import github.maxsuel.agregadordeinvestimentos.entity.*;
import github.maxsuel.agregadordeinvestimentos.exceptions.AccountNotFoundException;
import github.maxsuel.agregadordeinvestimentos.mapper.AccountStockMapper;
import github.maxsuel.agregadordeinvestimentos.repository.AccountRepository;
import github.maxsuel.agregadordeinvestimentos.repository.AccountStockRepository;
import github.maxsuel.agregadordeinvestimentos.repository.StockRepository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private AccountStockRepository accountStockRepository;

    @Mock
    private BrapiClient brapiClient;

    @Mock
    private AccountStockMapper accountStockMapper;

    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        accountService = new AccountService(
                accountRepository,
                stockRepository,
                accountStockRepository,
                brapiClient,
                accountStockMapper
        );
        ReflectionTestUtils.setField(accountService, "TOKEN", "test-token");
    }

    @NotNull
    @Contract("_, _ -> new")
    private StockDto createMockStockDto(String id, double price) {
        return new StockDto(
                id,
                "Name",
                "Long Name",
                price,
                0.0,
                0L,
                "BRL",
                "http://logo.url"
        );
    }

    @NotNull
    @Contract("_ -> new")
    private Stock createMockStockEntity(String id) {
        return new Stock(
                id,
                "Name",
                "Long Name",
                "Sector",
                "http://logo.url",
                "Description"
        );
    }

    @Nested
    @DisplayName("Stock Association & Balance.")
    public class AssociateAndBalanceTests {

        @Test
        @DisplayName("Should associate a stock to an account.")
        public void associateStock_Success() {
            // Arrange & Act
            var accountId = UUID.randomUUID();
            var dto = new AssociateAccountStockDto("ITUB4", 100);
            var account = new Account();
            account.setAccountId(accountId);
            var stock = createMockStockEntity("ITUB4");

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(stockRepository.findById("ITUB4")).thenReturn(Optional.of(stock));

            // Assert
            assertDoesNotThrow(() -> accountService.associateStock(accountId.toString(), dto));
            verify(accountStockRepository).save(any(AccountStock.class));
        }

        @Test
        @DisplayName("Should calculate account balance correctly.")
        public void getAccountBalance_Success() {
            // Arrange
            var accountId = UUID.randomUUID();
            var account = new Account(new User(), "Wallet", new ArrayList<>());
            account.setAccountId(accountId);
            var stock = createMockStockEntity("PETR4");
            account.getAccountStocks().add(new AccountStock(new AccountStockId(accountId, "PETR4"), account, stock, 10, BigDecimal.ZERO));

            var brapiResponse = new BrapiResponseDto(List.of(createMockStockDto("PETR4", 30.0)));

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(brapiClient.getQuote(anyString(), anyString())).thenReturn(brapiResponse);
            when(accountStockMapper.calculateTotal(anyDouble(), anyDouble())).thenReturn(300.0);

            // Act
            var result = accountService.getAccountBalance(accountId.toString());

            // Assert
            assertNotNull(result);
            assertEquals(300.0, result.totalBalance());
        }

    }

    @Nested
    @DisplayName("Portfolio and Security.")
    public class PortfolioTests {

        @Test
        @DisplayName("Should return complete portfolio summing cash and stocks.")
        public void getCompletePortfolio_Success() {
            // Arrange
            var userId = UUID.randomUUID();
            var accountId = UUID.randomUUID();
            var user = new User();
            user.setUserId(userId);
            user.setCash(new BigDecimal("500.00"));

            var account = new Account();
            account.setAccountId(accountId);
            account.setUser(user);
            account.setAccountStocks(new ArrayList<>());
            var stock = createMockStockEntity("AAPL");
            account.getAccountStocks().add(new AccountStock(null, account, stock, 2, BigDecimal.ZERO));

            // Act
            var brapiResponse = new BrapiResponseDto(List.of(createMockStockDto("AAPL", 150.0)));

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(brapiClient.getQuote(anyString(), anyString())).thenReturn(brapiResponse);

            var portfolio = accountService.getCompletePortfolio(user, accountId.toString());

            // Assert
            assertEquals(0, new BigDecimal("800.00").compareTo(portfolio.totalEquity()));
            assertEquals(0, new BigDecimal("300.00").compareTo(portfolio.investedInStocks()));
        }

        @Test
        @DisplayName("Should throw exception for unauthorized account access.")
        public void getCompletePortfolio_Forbidden() {
            // Arrange & Act
            var user = new User(); user.setUserId(UUID.randomUUID());
            var otherUser = new User(); otherUser.setUserId(UUID.randomUUID());
            var account = new Account(); account.setUser(otherUser);
            var accountId = UUID.randomUUID();

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            // Assert
            assertThrows(AccountNotFoundException.class, () -> accountService.getCompletePortfolio(user, accountId.toString()));
        }
    }

    @Nested
    @DisplayName("Resilience.")
    public class ResilienceTests {

        @Test
        @DisplayName("Should return fallback response with N/A and Service Unavailable.")
        public void fallbackListStocks_Success() {
            // Arrange & Act
            var result = accountService.fallbackListStocks("any-id", new RuntimeException());

            assertFalse(result.isEmpty());
            var fallbackDto = result.getFirst();

            // Assert
            assertEquals("N/A", fallbackDto.stockId());
        }

    }

}
