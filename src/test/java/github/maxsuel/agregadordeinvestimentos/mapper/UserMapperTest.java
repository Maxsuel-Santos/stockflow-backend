package github.maxsuel.agregadordeinvestimentos.mapper;

import github.maxsuel.agregadordeinvestimentos.dto.request.auth.CreateUserDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.auth.UserDto;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.entity.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Tests for User Mapper.")
public class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Nested
    @DisplayName("Tests for Mapping to Entity.")
    public class ToEntityMappingTest {

        @Test
        @DisplayName("Should map CreateUserDto and encoded password to User Entity with ADMIN role.")
        public void shouldMapToEntitySuccess() {
            // Arrange
            CreateUserDto dto = new CreateUserDto("username", "username@test.com", "password123");
            String encodedPassword = "encoded_hash_here";

            // Act
            User user = mapper.toEntity(dto, encodedPassword);

            // Assert
            assertThat(user).isNotNull();
            assertThat(user.getUsername()).isEqualTo(dto.username());
            assertThat(user.getEmail()).isEqualTo(dto.email());
            assertThat(user.getPassword())
                    .withFailMessage("The password in the entity must be the encoded one.")
                    .isEqualTo(encodedPassword);
            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
            assertThat(user.getUserId()).isNull();
            assertThat(user.getAccounts()).isNull();
        }

    }

    @Nested
    @DisplayName("Tests for Mapping to DTO.")
    public class ToDtoMappingTests {

        @Test
        @DisplayName("Should map User entity to UserDto correctly.")
        public void shouldMapToDtoSuccess() {
            // Arrange
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setUserId(id);
            user.setUsername("John Doe");
            user.setEmail("john_doe@test.com");

            // Act
            UserDto dto = mapper.toDto(user);

            // Assert
            assertThat(dto).isNotNull();
            assertThat(dto.userId()).isEqualTo(id.toString());
            assertThat(dto.username()).isEqualTo("John Doe");
            assertThat(dto.email()).isEqualTo("john_doe@test.com");
        }

    }

}
