package github.maxsuel.agregadordeinvestimentos.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to update user information.")
public record UpdateUserDto(

    @Schema(
        description = "New username to replace the current one.",
        example = "new_user"
    )
    String username,

    @Schema(
        description = "New user password with a minimum of 8 characters.",
        example = "NewPassword@2026"
    )
    @Size(min = 8, message = "If you change your password, it must be at least 8 characters long.")
    String password

) {
}
