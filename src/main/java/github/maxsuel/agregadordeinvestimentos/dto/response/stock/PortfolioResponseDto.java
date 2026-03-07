package github.maxsuel.agregadordeinvestimentos.dto.response.stock;

import java.math.BigDecimal;
import java.time.Instant;

import github.maxsuel.agregadordeinvestimentos.annotation.JsonBrasiliaTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Detailed breakdown of the user's financial standing.")
public record PortfolioResponseDto(

    @NotNull
    @Schema(
        description = "User's liquid cash available in the platform.",
        example = "1500.50"
    )
    BigDecimal availableCash,

    @NotNull
    @Schema(
        description = "Market value sum of all owned stocks.",
        example = "4250.75"
    )
    BigDecimal investedInStocks,

    @NotNull
    @Schema(
        description = "Total equity (cash + stocks).",
        example = "5751.25"
    )
    BigDecimal totalEquity,

    @Schema(
        description = "Timestamp of the real-time calculation."
    )
    @JsonBrasiliaTime
    Instant updatedAt

) {
}
