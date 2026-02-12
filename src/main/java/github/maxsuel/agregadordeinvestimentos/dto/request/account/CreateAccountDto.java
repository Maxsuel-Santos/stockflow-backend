package github.maxsuel.agregadordeinvestimentos.dto.request.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload to create a new account.")
public record CreateAccountDto(

    @Schema(
        description = "Human-readable description of the account.",
        example = "Carteira de dividendos"
    )
    @NotBlank(message = "Account description is required.")
    @NotNull(message = "Account description cannot be null.")
    String description,

    @Schema(
        description = "Street name of the account address.",
        example = "Avenida Paulista"
    )
    @NotBlank(message = "The street name is required.")
    @NotNull(message = "The street name cannot be null.")
    String street,

    @Schema(
        description = "Street number of the account address.",
        example = "1500"
    )
    @NotNull(message = "The address number is required.")
    @Min(value = 1, message = "The number must be positive.")
    Integer number

) {
}
