package github.maxsuel.agregadordeinvestimentos.controller;

import github.maxsuel.agregadordeinvestimentos.dto.request.stock.CreateStockDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.stock.UserStockSummaryDto;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.exceptions.dto.ErrorResponseDto;
import github.maxsuel.agregadordeinvestimentos.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
@Tag(name = "Stocks", description = "Managing the catalog of assets available in the system.")
public class StockController {

    private final StockService stockService;

    @Operation(
        summary = "Register a new stock in the catalog",
        description = "Adds a valid ticker (ex: AAPL, ITUB4) to the system's database so it can be traded."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Stock successfully registered."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid stock data or ticker already exists.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    @PostMapping
    public ResponseEntity<Void> createStock(@Valid @RequestBody CreateStockDto createStockDto) {
        stockService.createStock(createStockDto);

        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "List all owned stocks",
        description = "Calculates and returns a consolidated summary of all assets owned by the authenticated user, including total quantities and average prices.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "List of owned stocks retrieved successfully.",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = UserStockSummaryDto.class))
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid JWT token.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<UserStockSummaryDto>> getOwnedStocks(@Parameter(hidden = true) @AuthenticationPrincipal User user) {
        var ownedStocks = stockService.listOwnedStocks(user);

        return ResponseEntity.ok(ownedStocks);
    }

}
