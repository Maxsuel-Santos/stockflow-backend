package github.maxsuel.agregadordeinvestimentos.dto.response.auth;

import github.maxsuel.agregadordeinvestimentos.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Represents the authenticated user.")
public record UserDto(

    @NotNull
    @Schema(
        description = "Unique identifier of the user.",
        example = "a3f1c2e4-9b7d-4c1e-8f3a-12ab34cd56ef"
    )
    String userId,

    @NotNull
    @Schema(
        description = "Unique username used to identify the user in the system.",
        example = "john_doe"
    )
    String username,

    @NotNull
    @Schema(
        description = "User email address.",
        example = "johndoe@email.com"
    )
    String email,

    @NotNull
    @Schema(
        description = "Role or profile assigned to the user.",
        example = "ADMIN"
    )
    Role role,

    @Schema(
        description = "URL of the user's avatar image.",
        example = "http://example.com/avatars/johndoe.jpg"
    )
    String avatarUrl
) {
}
