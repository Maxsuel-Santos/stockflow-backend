package github.maxsuel.agregadordeinvestimentos.dto.response.stock;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary of a stock owned by the user.")
public record UserStockSummaryDto(

    @Schema(example = "ITUB4")
    String stockId,

    @Schema(
        description = "Total quantity owned across all accounts.",
        example = "150"
    )
    Integer totalQuantity

) {
}
