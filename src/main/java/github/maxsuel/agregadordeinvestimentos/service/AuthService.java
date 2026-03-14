package github.maxsuel.agregadordeinvestimentos.service;

import github.maxsuel.agregadordeinvestimentos.exceptions.DuplicatedDataException;
import github.maxsuel.agregadordeinvestimentos.mapper.UserMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import github.maxsuel.agregadordeinvestimentos.config.TokenService;
import github.maxsuel.agregadordeinvestimentos.dto.response.auth.AuthResponseDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.auth.CreateUserDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.auth.LoginDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.auth.UserDto;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final BlacklistService blacklistService;

    @Transactional
    public AuthResponseDto register(@NonNull CreateUserDto createUserDto) {
        if (userRepository.existsByUsername(createUserDto.username())) {
            throw new DuplicatedDataException("The username already exists.");
        }

        if (userRepository.existsByEmail(createUserDto.email())) {
            throw new DuplicatedDataException("Email already registered.");
        }

        String encodedPassword = passwordEncoder.encode(createUserDto.password());
        User entity = userMapper.toEntity(createUserDto, encodedPassword);

        var userSaved = userRepository.save(entity);
        log.info("User created with ID: {} and type {}", userSaved.getUserId(), userSaved.getRole());

        var token = tokenService.generateToken(userSaved);
        UserDto userDto = userMapper.toDto(userSaved);

        return new AuthResponseDto(token, userDto);
    }

    @Transactional
    public AuthResponseDto login(@NonNull LoginDto dto) {
        var user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        UserDto userDto = userMapper.toDto(user);
        var token = tokenService.generateToken(user);

        return new AuthResponseDto(token, userDto);
    }

    public UserDto getAuthenticatedUserDto(User user) {
        if (user == null) {
            throw new BadCredentialsException("User not authenticated");
        }

        return userMapper.toDto(user);
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "");
            blacklistService.blacklistToken(token);

            log.info("Token added to blacklist.");
        }
    }

}
