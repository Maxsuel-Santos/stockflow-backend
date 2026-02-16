package github.maxsuel.agregadordeinvestimentos.controller;


import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountBalanceDto;
import github.maxsuel.agregadordeinvestimentos.exceptions.dto.ErrorResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import github.maxsuel.agregadordeinvestimentos.dto.request.account.AssociateAccountStockDto;
import github.maxsuel.agregadordeinvestimentos.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(
    name = "Accounts", 
    description = "Endpoints for portfolio management and stock association."
)
public class AccountController {

    private final AccountService accountService;

    @Operation(
        summary = "Associate a stock with an account",
        description = "Links a financial asset (ticker) and the quantity purchased to a specific account. If the asset is already associated, it updates the position.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Stock associated successfully.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data (e.g., negative quantity or invalid ticker format).",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Account or Stock not found in the system.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        )
    })
    @PostMapping("/{accountId}/stocks")
    public ResponseEntity<Void> associateStock(@PathVariable("accountId") String accountId,
                                               @Valid @RequestBody AssociateAccountStockDto dto) {
        accountService.associateStock(accountId, dto);

        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Get account total balance",
        description = "Calculates the total equity by summing all stocks owned by the account, using real-time prices from the Brapi API.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Balance calculated successfully.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AccountBalanceDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Account not found.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "502",
            description = "Bad Gateway - Error communicating with Brapi API.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<AccountBalanceDto> getBalance(@PathVariable String accountId) {
        return ResponseEntity.ok(accountService.getAccountBalance(accountId));
    }

}
