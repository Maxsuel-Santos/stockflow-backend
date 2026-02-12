package github.maxsuel.agregadordeinvestimentos.exceptions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Standardized object for returning API error details.")
public record ErrorResponseDto(

    @NotNull
    @Schema(
        example = "400",
        description = "HTTP Status Code."
    )
    int status,

    @NotNull
    @Schema(
        example = "Validation failed for one or more fields.",
        description = "General error message"
    )
    String message,

    @NotNull
    @Schema(description = "Timestamp when the error occurred")
    Instant timestamp,

    @Schema(
        example = "{\"username\": \"Username cannot be empty\", \"email\": \"Invalid email format\"}",
        description = "Map containing specific field names and their respective validation error messages. Can be null if not a validation error."
    )
    Map<String, String> fieldsErrors

) {
}
