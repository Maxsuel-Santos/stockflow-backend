package github.maxsuel.agregadordeinvestimentos.dto.response.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Summary of a stock owned by the user.")
public record UserStockSummaryDto(

    @NotNull
    @Schema(
        description = "Ticker symbol of the stock.",
        example = "ITUB4"
    )
    String stockId,

    @NotNull
    @Schema(
        description = "Total quantity owned across all accounts.",
        example = "150"
    )
    Integer totalQuantity

) {
}
