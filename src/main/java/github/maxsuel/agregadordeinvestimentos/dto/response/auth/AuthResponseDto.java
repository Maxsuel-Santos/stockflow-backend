package github.maxsuel.agregadordeinvestimentos.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Response after a successful login.")
public record AuthResponseDto(

    @NotNull
    @Schema(
        description = "JWT token used for authenticated requests.",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    String accessToken,

    @NotNull
    @Schema(
        description = "The authenticated user details.",
        implementation = UserDto.class
    )
    UserDto user

) {
}
