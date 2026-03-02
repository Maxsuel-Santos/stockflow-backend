package github.maxsuel.agregadordeinvestimentos.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountResponseDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.account.CreateAccountDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountStockResponseDto;
import github.maxsuel.agregadordeinvestimentos.exceptions.InvalidFileException;
import github.maxsuel.agregadordeinvestimentos.mapper.AccountMapper;
import github.maxsuel.agregadordeinvestimentos.repository.AccountRepository;
import github.maxsuel.agregadordeinvestimentos.service.storage.StorageService;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import github.maxsuel.agregadordeinvestimentos.dto.request.user.UpdateUserDto;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.exceptions.UserNotFoundException;
import github.maxsuel.agregadordeinvestimentos.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;
    private final StorageService storageService;

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(UUID.fromString(userId));
    }

    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void updateUserById(String userId, @NonNull UpdateUserDto updateUserDto) {
        var userUuid = UUID.fromString(userId);

        var user = userRepository.findById(userUuid)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (updateUserDto.username() != null) {
            user.setUsername(updateUserDto.username());
        }

        if (updateUserDto.password() != null) {
            user.setPassword(passwordEncoder.encode(updateUserDto.password()));
        }

        userRepository.save(user);
        log.info("User updated with ID: {}. Password re-hashed.", userId);
    }

    @Transactional
    public void deleteUser(String userId) {
        var userUuid = UUID.fromString(userId);

        var userExists = userRepository.existsById(userUuid);

        if (userExists) {
            userRepository.deleteById(userUuid);
            log.info("User deleted with ID: {}", userId);
        } else {
            log.warn("Attempted to delete non-existing user with ID: {}", userId);
        }
    }

    @Transactional
    public void createAccount(String userId, CreateAccountDto createAccountDto) {
        var user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        var account = accountMapper.toEntity(createAccountDto, user);

        accountRepository.save(account);

        log.info("Account and BillingAddress created for user: {}", userId);
    }

    public List<AccountResponseDto> listAllAccounts(String userId) {
        var user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return user.getAccounts()
                .stream()
                .map(account -> {
                    List<AccountStockResponseDto> stocks = accountService.listAllStocks(account.getAccountId().toString());

                    return accountMapper.toDto(account, stocks);
                })
                .toList();
    }

    @Transactional
    public void uploadAvatar(UUID userId, MultipartFile file) {
        validateImage(file);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            storageService.deleteFile(user.getAvatarUrl());
        }

        String newUrl = storageService.uploadFile(file);

        user.setAvatarUrl(newUrl);
        userRepository.save(user);

        log.info("Avatar updated for user: {}", userId);
    }

    @Transactional
    public void deleteAvatar(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            storageService.deleteFile(user.getAvatarUrl());

            user.setAvatarUrl(null);
            userRepository.save(user);
            log.info("Avatar removed for user: {}", userId);
        }

    }

    private void validateImage(@NotNull MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Cannot upload an empty file.");
        }

        long maxSize = 5242880;
        if (file.getSize() > maxSize) {
            throw new InvalidFileException("File size exceeds the limit of 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isSupportedContentType(contentType)) {
            throw new InvalidFileException("Only JPG, PNG and WEBP images are allowed.");
        }
    }

    private boolean isSupportedContentType(@NotNull String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/webp");
    }

}
