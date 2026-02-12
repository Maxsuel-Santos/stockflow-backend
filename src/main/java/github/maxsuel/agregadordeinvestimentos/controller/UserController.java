package github.maxsuel.agregadordeinvestimentos.controller;

import java.util.List;

import github.maxsuel.agregadordeinvestimentos.exceptions.dto.ErrorResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(
    name = "Users", 
    description = "Endpoints for users management"
)
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "Search user by ID",
        description = "Retrieves full details of a specific user. Use this to fetch profile information."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User found successfully.",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User ID does not exist in the database.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable("userId") String userId) {
        var user = userService.getUserById(userId);

        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "List all registered users",
        description = "Administrative endpoint to retrieve a list of every user in the system."
    )
    @ApiResponse(
        responseCode = "200",
        description = "List of users retrieved successfully.",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))
    )
    @GetMapping("/all")
    public ResponseEntity<List<User>> listAllUsers() {
        return ResponseEntity.ok(userService.listAllUsers());
    }

    @Operation(
        summary = "Update user data",
        description = "Modifies existing user information (e.g., name or email). Only provided fields will be updated."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User updated successfully."),
        @ApiResponse(
            responseCode = "404",
            description = "User not found.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid update data.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
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
        description = "Deletes a user based on the provided user ID."
    )
    @ApiResponse(
        responseCode = "204",
        description = "User successfully deleted.",
        content = @Content
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Create an account for the user",
        description = "Initializes a new investment portfolio (Account) for a specific user."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account successfully created."),
        @ApiResponse(
            responseCode = "404",
            description = "User not found.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
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
        description = "Retrieves all accounts owned by the user, including consolidated stock portfolios and real-time market values."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Accounts and portfolios retrieved successfully.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountResponseDto.class)))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found.",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    @GetMapping("/{userId}/accounts")
    public ResponseEntity<List<AccountResponseDto>> listAllAccounts(@PathVariable("userId") String userId) {
        var accounts = userService.listAllAccounts(userId);
        return ResponseEntity.ok(accounts);
    }
}
