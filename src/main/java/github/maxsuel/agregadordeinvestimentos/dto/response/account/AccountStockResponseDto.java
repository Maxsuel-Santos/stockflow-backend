package github.maxsuel.agregadordeinvestimentos.dto.response.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Represents a stock position in the user's portfolio.")
public record AccountStockResponseDto(

    @NotNull
    @Schema(
        description = "Stock ticker symbol.",
        example = "ITUB4"
    )
    String stockId,

    @NotNull
    @Schema(
        description = "Name of the company issuing the stock.",
        example = "Itaú Unibanco"
    )
    String name,

    @NotNull
    @Schema(
        description = "Full name of the company issuing the stock.",
        example = "Banco Itaú Unibanco PN"
    )
    String longName,

    @NotNull
    @Schema(
        description = "Industry",
        example = "Energy"
    )
    String sector,

    @NotNull
    @Schema(
        description = "Total quantity of the stock in the portfolio.",
        example = "100"
    )
    int quantity,

    @NotNull
    @Schema(
        description = "Average purchase price (User cost)",
        example = "32.45"
    )
    double avgPrice,

    @NotNull
    @Schema(
        description = "Current market price (Brapi)",
        example = "37.70"
    )
    double currentPrice,

    @NotNull
    @Schema(
        description = "Percentage change on the day",
        example = "-0.29"
    )
    double change,

    @NotNull
    @Schema(
        description = "Daily trading volume",
        example = "489500"
    )
    long volume,

    @NotNull
    @Schema(
        description = "Total value at market price (Qty * CurrentPrice)",
        example = "5655.00"
    )
    double marketValue,

    @NotNull
    @Schema(
        description = "Total amount invested in the stock (quantity x average price).",
        example = "3245.00"
    )
    double total,

    @NotNull
    @Schema(
        description = "URL of the logo of the company issuing the stock.",
        example = "https://icons.brapi.dev/icons/ITUB4.svg"
    )
    String logoUrl

) {
}
