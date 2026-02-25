package github.maxsuel.agregadordeinvestimentos.dto.external.brapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary profile of the company.")
public record SummaryProfileDto(
    @Schema(
        description = "Sector of the company.",
        example = "Energy"
    )
    String sector
) {
}
