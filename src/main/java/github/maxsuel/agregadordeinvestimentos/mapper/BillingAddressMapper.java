package github.maxsuel.agregadordeinvestimentos.mapper;

import github.maxsuel.agregadordeinvestimentos.dto.request.account.CreateAccountDto;
import github.maxsuel.agregadordeinvestimentos.entity.Account;
import github.maxsuel.agregadordeinvestimentos.entity.BillingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BillingAddressMapper {

    @Mapping(target = "id", source = "account.accountId")
    @Mapping(target = "account", source = "account")
    @Mapping(target = "street", source = "dto.street")
    @Mapping(target = "number", source = "dto.number")
    BillingAddress toEntity(CreateAccountDto dto, Account account);

}
