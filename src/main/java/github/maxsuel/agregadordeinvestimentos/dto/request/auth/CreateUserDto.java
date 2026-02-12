package github.maxsuel.agregadordeinvestimentos.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to create a new user.")
public record CreateUserDto(

    @Schema(
        description = "Unique username used to identify the user in the system.",
        example = "john_doe"
    )
    @NotBlank(message = "Username is required.")
    @NotNull(message = "Username cannot be null.")
    String username,

    @Schema(
        description = "User email address.",
        example = "johndoe@email.com"
    )
    @NotBlank(message = "Email is required.")
    @NotNull(message = "Email cannot be null.")
    @Email(message = "The email address must be valid.")
    String email,

    @Schema(
        description = "User password with a minimum of 8 characters.",
        example = "password#123"
    )
    @NotBlank(message = "Password is mandatory.")
    @NotNull(message = "Password cannot be null.")
    @Size(min = 8, message = "The password must be at least 8 characters long.")
    String password

) {
}
