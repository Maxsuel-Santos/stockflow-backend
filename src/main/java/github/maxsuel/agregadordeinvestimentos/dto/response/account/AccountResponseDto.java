package github.maxsuel.agregadordeinvestimentos.dto.response.account;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Response with user account details.")
public record AccountResponseDto(

    @NotNull
    @Schema(
        description = "Unique identifier of the account.",
        example = "e87e1f43-8f0a-4a2b-8a8b-1234567890ab"
    )
    String accountId,

    @NotNull
    @Schema(
        description = "Friendly description of the account.",
        example = "Main account"
    )
    String description,

    @ArraySchema(
        schema = @Schema(
            implementation = AccountStockResponseDto.class,
            description = "List of stocks held in this account with real-time prices."
        )
    )
    List<AccountStockResponseDto> stocks

) {
}
