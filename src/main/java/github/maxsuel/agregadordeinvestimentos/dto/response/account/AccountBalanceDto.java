package github.maxsuel.agregadordeinvestimentos.dto.response.account;

import java.time.Instant;

import github.maxsuel.agregadordeinvestimentos.annotation.JsonBrasiliaTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data Transfer Object representing the total financial balance of an account.")
public record AccountBalanceDto(

    @NotNull
    @Schema(
        description = "Unique identifier of the account (UUID).",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    String accountId,

    @NotNull
    @Schema(
        description = "The sum of all assets in the account, multiplied by their current market price.",
        example = "15450.75"
    )
    Double totalBalance,

    @NotNull
    @Schema(
        description = "The exact moment the balance was calculated based on real-time data.",
        example = "2026-01-26T14:48:19Z"
    )
    @JsonBrasiliaTime
    Instant updatedAt

) {
}
