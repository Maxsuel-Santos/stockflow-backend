package github.maxsuel.agregadordeinvestimentos.dto.response.stock;

import github.maxsuel.agregadordeinvestimentos.annotation.JsonBrasiliaTime;
import github.maxsuel.agregadordeinvestimentos.entity.enums.TradeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "TransactionResponse",
    description = "Model representing a historical stock market transaction (Buy/Sell) for account statements."
)
public record TransactionsResponseDto(

    @Schema(
        description = "Unique identifier of the transaction in the database.",
        example = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    )
    @NotNull
    UUID transactionId,

    @Schema(
        description = "Ticker symbol of the asset traded.",
        example = "BBAS3"
    )
    @NotBlank
    String stockId,

    @Schema(
        description = "Type of the trade operation.",
        example = "BUY"
    )
    @NotNull
    TradeType type,

    @Schema(
        description = "Number of units (shares) traded in this specific operation.",
        example = "100"
    )
    @NotNull
    Integer quantity,

    @Schema(
        description = "Unit price of the asset at the exact time of the transaction.",
        example = "34.50"
    )
    @NotNull
    BigDecimal priceAtTime,

    @Schema(
        description = "Total monetary value of the operation (Quantity * PriceAtTime).",
        example = "3450.00"
    )
    @NotNull
    @PositiveOrZero
    BigDecimal totalValue,

    @Schema(
        description = "The precise date and time when the transaction was processed and persisted.",
        example = "2026-02-10T15:30:00Z"
    )
    @NotNull
    @JsonBrasiliaTime
    Instant timestamp

) {
}
