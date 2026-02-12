package github.maxsuel.agregadordeinvestimentos.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data required to authenticate a user and generate a JWT access token.")
public record LoginDto(

    @Schema(
        example = "username",
        description = "The unique username used during registration.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Username cannot be empty.")
    @NotNull(message = "Username cannot be null.")
    String username,

    @Schema(
        example = "P@ssw0rd123",
        description = "The user's password in plain text. It will be validated against the stored hash.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "password"
    )
    @NotBlank(message = "Password cannot be empty.")
    @NotNull(message = "Password cannot be null.")
    String password

) {
}
