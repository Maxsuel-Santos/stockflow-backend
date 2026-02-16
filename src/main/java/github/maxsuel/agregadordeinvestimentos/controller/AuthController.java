package github.maxsuel.agregadordeinvestimentos.controller;

import java.net.URI;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import github.maxsuel.agregadordeinvestimentos.dto.request.auth.CreateUserDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.auth.LoginDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.auth.AuthResponseDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.auth.UserDto;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.exceptions.dto.ErrorResponseDto;
import github.maxsuel.agregadordeinvestimentos.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication", 
    description = "Endpoints for user authentication and registration"
)
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Create/Register a new user.", 
        description="Registers a new user and returns a JWT token for authentication."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "User created successfully", 
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponseDto.class)
            )   
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or email already exists",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody CreateUserDto createUserDto) {
        var registerResponse = authService.register(createUserDto);
        return ResponseEntity.created(URI.create("/users/" + registerResponse.user().userId())).body(registerResponse);
    }

    @Operation(
        summary = "Log in and receive the JWT token.",
            description = "Validates credentials and generates a Bearer token to authenticate subsequent requests."
        )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Login successfully", 
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponseDto.class)
            )   
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid email or password",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        var loginResponse = authService.login(loginDto);
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
        summary = "Get current authenticated user",
        description = "Extracts user data from the JWT token provided in the Authorization header.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User data retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token is missing, expired or invalid",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@Parameter(hidden = true) @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var userDto = authService.getAuthenticatedUserDto(user);

        return ResponseEntity.ok(userDto);
    }

    @Operation(
        summary = "Logout user",
        description = "Invalidates the current Bearer token. Requires an active session.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Logged out successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@NonNull HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        authService.logout(authHeader);

        return ResponseEntity.noContent().build();
    }

}
