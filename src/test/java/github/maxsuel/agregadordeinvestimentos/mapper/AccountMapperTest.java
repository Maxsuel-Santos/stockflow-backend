package github.maxsuel.agregadordeinvestimentos.mapper;

import github.maxsuel.agregadordeinvestimentos.dto.request.account.CreateAccountDto;
import github.maxsuel.agregadordeinvestimentos.entity.Account;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests for Account Mapper.")
class AccountMapperTest {

    private final AccountMapper mapper = Mappers.getMapper(AccountMapper.class);

    @Nested
    @DisplayName("Tests for Mapping to Entity.")
    class ToEntityMappingTests {

        @Test
        @DisplayName("Should map CreateAccountDto and User to Account Entity with BillingAddress correctly.")
        void shouldMapToEntityWithAddressSuccess() {
            // Arrange
            var description = "Dividendos";
            var street = "Rua A";
            var number = 100;
            var username = "user_test";

            CreateAccountDto dto = new CreateAccountDto(description, street, number);
            User user = new User();
            user.setUsername(username);

            // Act
            Account account = mapper.toEntity(dto, user);

            // Assert
            assertThat(account).isNotNull();
            assertThat(account.getDescription()).isEqualTo(description);
            assertThat(account.getUser()).isNotNull();
            assertThat(account.getUser().getUsername()).isEqualTo(username);

            assertThat(account.getBillingAddress()).isNotNull();
            assertThat(account.getBillingAddress().getStreet()).isEqualTo(street);
            assertThat(account.getBillingAddress().getNumber()).isEqualTo(number);

            assertThat(account.getBillingAddress().getAccount())
                    .withFailMessage("The relationship between BillingAddress and Account must be linked.")
                    .isEqualTo(account);

            assertThat(account.getAccountStocks())
                    .isNotNull()
                    .isEmpty();
        }

    }

}
