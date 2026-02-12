package github.maxsuel.agregadordeinvestimentos.dto.external.brapi;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned by the Brapi external API containing stock data.")
public record BrapiResponseDto(

    @Schema(
        description = "List of stock results returned by the external API.",
        example = """
        [
          {
            "stockId": "PETR4",
            "name": "Petrobras",
            "price": 37.50
          }
        ]
        """
    )
    List<StockDto> results

) {
}
