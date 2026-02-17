package github.maxsuel.agregadordeinvestimentos.dto.response.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Summary of a stock owned by the user.")
public record UserStockSummaryDto(

    @Schema(example = "ITUB4")
    @NotNull
    String stockId,

    @Schema(
        description = "Total quantity owned across all accounts.",
        example = "150"
    )
    @NotNull
    Integer totalQuantity

) {
}
