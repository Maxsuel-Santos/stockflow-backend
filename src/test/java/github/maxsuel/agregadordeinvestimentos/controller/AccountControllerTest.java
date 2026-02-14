package github.maxsuel.agregadordeinvestimentos.controller;

import github.maxsuel.agregadordeinvestimentos.dto.request.account.AssociateAccountStockDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountBalanceDto;
import github.maxsuel.agregadordeinvestimentos.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @Nested
    @DisplayName("Tests for Associate Stock")
    public class AssociateStockTests {

        @Test
        @DisplayName("Should return 200 OK when stock is associated successfully.")
        public void shouldAssociateStockWithSuccess() {
            // Arrange
            var accountId = UUID.randomUUID().toString();
            var dto = new AssociateAccountStockDto("PETR4", 50);

            doNothing().when(accountService).associateStock(accountId, dto);

            // Act
            var response = accountController.associateStock(accountId, dto);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());

            verify(accountService, times(1)).associateStock(accountId, dto);
        }

    }

    @Nested
    @DisplayName("Tests for Account Balance")
    public class AccountBalanceTests {

        @Test
        @DisplayName("Should return 200 OK and current balance")
        public void shouldReturnBalanceWithSuccess() {
            // Arrange
            var accountId = UUID.randomUUID().toString();
            var now = Instant.now();
            var expectedBalance = new AccountBalanceDto(
                    accountId,
                    12500.50,
                    now
            );

            when(accountService.getAccountBalance(accountId)).thenReturn(expectedBalance);

            // Act
            var response = accountController.getBalance(accountId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(12500.50, response.getBody().totalBalance());
            assertEquals(accountId, response.getBody().accountId());
            assertEquals(now, response.getBody().updatedAt());

            verify(accountService, times(1)).getAccountBalance(accountId);
        }

    }

}
