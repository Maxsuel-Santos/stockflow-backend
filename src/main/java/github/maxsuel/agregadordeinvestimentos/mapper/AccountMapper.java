package github.maxsuel.agregadordeinvestimentos.mapper;

import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountResponseDto;
import github.maxsuel.agregadordeinvestimentos.dto.request.account.CreateAccountDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountStockResponseDto;
import github.maxsuel.agregadordeinvestimentos.entity.Account;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "description", source = "createAccountDto.description")
    @Mapping(target = "accountStocks", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "billingAddress.street", source = "createAccountDto.street")
    @Mapping(target = "billingAddress.number", source = "createAccountDto.number")
    @Mapping(target = "billingAddress.account", ignore = true)
    Account toEntity(CreateAccountDto createAccountDto, User user);

    @Mapping(target = "accountId", source = "account.accountId")
    @Mapping(target = "description", source = "account.description")
    @Mapping(target = "stocks", source = "stocks")
    AccountResponseDto toDto(Account account, List<AccountStockResponseDto> stocks);

    @AfterMapping
    default void linkAddress(@MappingTarget @NonNull Account account) {
        if (account.getBillingAddress() != null) {
            account.getBillingAddress().setAccount(account);
        }
    }
}
