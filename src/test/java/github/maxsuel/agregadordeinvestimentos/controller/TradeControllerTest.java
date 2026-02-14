package github.maxsuel.agregadordeinvestimentos.controller;

import github.maxsuel.agregadordeinvestimentos.dto.request.stock.TradeRequestDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.stock.PortfolioResponseDto;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.mapper.TransactionMapper;
import github.maxsuel.agregadordeinvestimentos.repository.TransactionsRepository;
import github.maxsuel.agregadordeinvestimentos.service.AccountService;
import github.maxsuel.agregadordeinvestimentos.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TradeControllerTest {

    @Mock
    private TradeService tradeService;

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TradeController tradeController;

    private User mockUser;
    private TradeRequestDto tradeRequestDto;
    private UUID accountId;

    @BeforeEach
    public void setUp() {
        mockUser = new User();

        accountId = UUID.randomUUID();

        tradeRequestDto = new TradeRequestDto("ITUB4", 10, accountId);
    }

    @Nested
    @DisplayName("Tests for Buy/Sell Operations.")
    public class TradeOperations {

        @Test
        @DisplayName("Should return 200 OK on successful buy.")
        public void shouldBuyWithSuccess() {
            // Arrange
            doNothing().when(tradeService).executeBuy(any(User.class), any(TradeRequestDto.class));

            // Act
            var response = tradeController.buy(tradeRequestDto, mockUser);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(tradeService, times(1)).executeBuy(mockUser, tradeRequestDto);
        }

        @Test
        @DisplayName("Should return 200 OK on successful sell.")
        public void shouldSellWithSuccess() {
            // Arrange
            doNothing().when(tradeService).executeSell(any(User.class), any(TradeRequestDto.class));

            // Act
            var response = tradeController.sell(tradeRequestDto, mockUser);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(tradeService, times(1)).executeSell(mockUser, tradeRequestDto);
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user is not present.")
        public void shouldReturn401WhenUserIsNull() {
            // Arrange & Act
            var responseBuy = tradeController.buy(tradeRequestDto, null);
            var responseSel = tradeController.sell(tradeRequestDto, null);

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, responseBuy.getStatusCode());
            assertEquals(HttpStatus.UNAUTHORIZED, responseSel.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Tests for History and Portfolio.")
    public class InsightsOperations {

        @Test
        @DisplayName("Should return transactions history.")
        public void  shouldReturnHistory() {
            // Arrange & Act
            when(transactionsRepository.findAllByUser_UserId(any())).thenReturn(Collections.emptyList());

            var response = tradeController.getHistory(mockUser);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            verify(transactionsRepository, times(1)).findAllByUser_UserId(any());
        }

        @Test
        @DisplayName("Should return consolidated portfolio")
        public void shouldReturnPortfolio() {
            // Arrange
            var accountId = UUID.randomUUID().toString();
            var now = Instant.now();
            var portfolioDto = new PortfolioResponseDto(
                    new BigDecimal("1500.50"),
                    new BigDecimal("4250.75"),
                    new BigDecimal("5751.25"),
                    now
            );

            when(accountService.getCompletePortfolio(mockUser, accountId)).thenReturn(portfolioDto);

            // Act
            var response = tradeController.getPortfolio(accountId, mockUser);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(new BigDecimal("5751.25"), response.getBody().totalEquity());
            assertEquals(now, response.getBody().updatedAt());
        }

    }

}
