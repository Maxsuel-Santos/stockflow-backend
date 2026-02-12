package github.maxsuel.agregadordeinvestimentos.service;

import github.maxsuel.agregadordeinvestimentos.dto.request.stock.CreateStockDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.stock.UserStockSummaryDto;
import github.maxsuel.agregadordeinvestimentos.entity.AccountStock;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.exceptions.UserNotFoundException;
import github.maxsuel.agregadordeinvestimentos.mapper.StockMapper;
import github.maxsuel.agregadordeinvestimentos.repository.StockRepository;
import github.maxsuel.agregadordeinvestimentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final StockMapper stockMapper;

    @Transactional
    public void createStock(@NonNull CreateStockDto createStockDto) {
        var stock = stockMapper.toEntity(createStockDto);

        stockRepository.save(stock);
    }

    @Transactional(readOnly = true)
    public List<UserStockSummaryDto> listOwnedStocks(@NonNull User user) {
        var userWithAccounts = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        return userWithAccounts.getAccounts().stream()
                .flatMap(account -> account.getAccountStocks().stream())
                .collect(Collectors.groupingBy(
                        as -> as.getStock().getStockId(),
                        Collectors.summingInt(AccountStock::getQuantity)
                ))
                .entrySet().stream()
                .map(entry -> new UserStockSummaryDto(entry.getKey(), entry.getValue()))
                .toList();
    }

}
