package github.maxsuel.agregadordeinvestimentos.controller;

import java.util.List;
import java.util.UUID;

import github.maxsuel.agregadordeinvestimentos.exceptions.UserNotFoundException;
import github.maxsuel.agregadordeinvestimentos.exceptions.dto.ErrorResponseDto;
import github.maxsuel.agregadordeinvestimentos.repository.UserRepository;
import github.maxsuel.agregadordeinvestimentos.service.storage.StorageService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountResponseDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.account.CreateAccountDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.user.UpdateUserDto;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(
    name = "Users", 
    description = "Endpoints for users management"
)
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Operation(
        summary = "Search user by ID",
        description = "Retrieves full details of a specific user. Use this to fetch profile information.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User found successfully.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = User.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User ID does not exist in the database.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable("userId") String userId) {
        var user = userService.getUserById(userId);

        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "List all registered users",
        description = "Administrative endpoint to retrieve a list of every user in the system.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
        responseCode = "200",
        description = "List of users retrieved successfully.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = User.class))
        )
    )
    @GetMapping("/all")
    public ResponseEntity<List<User>> listAllUsers() {
        return ResponseEntity.ok(userService.listAllUsers());
    }

    @Operation(
        summary = "Update user data",
        description = "Modifies existing user information (e.g., name or email). Only provided fields will be updated.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User updated successfully."),
        @ApiResponse(
            responseCode = "404",
            description = "User not found.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid update data.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUserById(@PathVariable("userId") String userId,
                                               @Valid @RequestBody UpdateUserDto updateUserDto) {
        userService.updateUserById(userId, updateUserDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Remove a user from the system.",
        description = "Deletes a user based on the provided user ID.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
        responseCode = "204",
        description = "User successfully deleted.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE
        )
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Create an account for the user",
        description = "Initializes a new investment portfolio (Account) for a specific user.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account successfully created."),
        @ApiResponse(
            responseCode = "404",
            description = "User not found.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    @PostMapping("/{userId}/accounts")
    public ResponseEntity<Void> createAccount(@PathVariable("userId") String userId,
                                              @Valid @RequestBody CreateAccountDto createAccountDto) {
        userService.createAccount(userId, createAccountDto);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "List all user accounts",
        description = "Retrieves all accounts owned by the user, including consolidated stock portfolios and real-time market values.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Accounts and portfolios retrieved successfully.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = AccountResponseDto.class))
        )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    @GetMapping("/{userId}/accounts")
    public ResponseEntity<List<AccountResponseDto>> listAllAccounts(@PathVariable("userId") String userId) {
        var accounts = userService.listAllAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @Operation(
        summary = "Upload user profile picture",
        description = "Uploads a new profile picture for the specified user and saves the URL in the database. If a previous picture exists, it will be deleted from storage.",
        operationId = "uploadUserAvatar"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Avatar uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or user ID",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error during file processing",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping(
        value = "/{userId}/avatar",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> uploadAvatar(@Parameter(description = "ID of the user to update") @PathVariable UUID userId,
                                             @Parameter(
                                                 description = "The image file to upload (JPEG, PNG, WEBP)",
                                                 content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
                                             )
                                             @RequestParam("file") MultipartFile file) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        // Delete old photo
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            storageService.deleteFile(user.getAvatarUrl());
        }

        String newUrl = storageService.uploadFile(file);

        user.setAvatarUrl(newUrl);
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

}
