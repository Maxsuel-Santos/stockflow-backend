package github.maxsuel.agregadordeinvestimentos.dto.external.brapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Stock market data returned by the external Brapi API.")
public record StockDto(

    @JsonProperty("symbol")
    @Schema(
        description = "Unique identifier (ticker symbol) of the stock.",
        example = "PETR4"
    )
    String stock,

    @Schema(
        description = "Short display name of the company or stock.",
        example = "Petrobras PN"
    )
    String shortName,

    @Schema(
        description = "Long display name of the company or stock.",
        example = "Petroleo Brasileiro SA Pfd"
    )
    String longName,

    @Schema(
        description = "Current market price of the stock.",
        example = "38.50",
        format = "double"
    )
    double regularMarketPrice,

    @Schema(
        description = "Percentage change in the regular market session.",
        example = "1.25"
    )
    double regularMarketChangePercent,

    @Schema(
        description = "Number of shares traded during the session.",
        example = "45000000"
    )
    long regularMarketVolume,

    @Schema(
        description = "Currency used for the stock price.",
        example = "BRL"
    )
    String currency,

    @Schema(
        description = "URL of the company logo.",
        example = "https://icons.brapi.dev/icons/PETR4.svg"
    )
    @JsonProperty("logourl")
    String logourl,

    @Schema(
        description = "Profile information containing the sector.",
        example = "Tecnology"
    )
    @JsonProperty("summaryProfile")
    SummaryProfileDto summaryProfile

) {
}
