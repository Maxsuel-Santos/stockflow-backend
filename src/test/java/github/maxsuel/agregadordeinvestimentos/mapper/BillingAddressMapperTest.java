package github.maxsuel.agregadordeinvestimentos.mapper;

import github.maxsuel.agregadordeinvestimentos.dto.request.account.CreateAccountDto;
import github.maxsuel.agregadordeinvestimentos.entity.Account;
import github.maxsuel.agregadordeinvestimentos.entity.BillingAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests for BillingAddress Mapper.")
public class BillingAddressMapperTest {

    private final BillingAddressMapper mapper = Mappers.getMapper(BillingAddressMapper.class);

    @Nested
    @DisplayName("Tests for Mapping to Entity.")
    public class ToEntityMappingTests {

        @Test
        @DisplayName("Should map CreateAccountDto and Account to BillingAddress with Shared ID.")
        public void shouldMapToEntitySuccess() {
            // Arrange
            var accountId = UUID.randomUUID();
            var account = new Account();
            account.setAccountId(accountId);

            var dto = new CreateAccountDto("Minha Conta", "Rua das Flores", 123);

            // Act
            BillingAddress result = mapper.toEntity(dto, account);

            // Assert
            assertThat(result).isNotNull();

            assertThat(result.getId())
                    .withFailMessage("The BillingAddress ID must match the Account ID.")
                    .isEqualTo(accountId);

            assertThat(result.getStreet()).isEqualTo("Rua das Flores");
            assertThat(result.getNumber()).isEqualTo(123);

            // Valida o vínculo do relacionamento
            assertThat(result.getAccount()).isEqualTo(account);
        }

    }

}
