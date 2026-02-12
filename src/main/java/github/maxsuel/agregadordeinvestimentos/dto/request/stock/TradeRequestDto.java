package github.maxsuel.agregadordeinvestimentos.dto.request.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(
    name = "TradeRequest",
    description = "Parameters required to perform an asset trading operation (Buy or Sell)."
)
public record TradeRequestDto(

    @Schema(
        description = "Asset symbol on the stock exchange (ticker). It must be a valid ticker on Brapi.",
        example = "PETR4",
        minLength = 3,
        maxLength = 10
    )
    @NotBlank(message = "The stock ticker cannot be empty.")
    @Size(min = 3, max = 10)
    String stockId,

    @Schema(
        description = "Number of shares to be traded. Must be a positive integer.",
        example = "100",
        minimum = "1"
    )
    @NotNull(message = "Quantity is mandatory")
    @Min(value = 1, message = "The minimum amount to trade is 1 share.")
    Integer quantity,

    @Schema(
        description = "Unique ID of the wallet (Account) where custody will be changed.",
        example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    )
    @NotNull(message = "The destination account ID is required.")
    UUID accountId

) {
}
