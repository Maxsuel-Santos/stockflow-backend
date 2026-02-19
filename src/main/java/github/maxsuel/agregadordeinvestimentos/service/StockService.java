package github.maxsuel.agregadordeinvestimentos.service;

import github.maxsuel.agregadordeinvestimentos.dto.request.stock.CreateStockDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountStockResponseDto;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    public List<AccountStockResponseDto> listOwnedStocks(@NonNull User user) {
        var userWithAccounts = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        return userWithAccounts.getAccounts().stream()
                .flatMap(account -> account.getAccountStocks().stream())
                .collect(Collectors.groupingBy(AccountStock::getStock))
                .entrySet().stream()
                .map(entry -> {
                    var stock = entry.getKey();
                    var positions = entry.getValue();

                    int totalQuantity = positions.stream()
                            .mapToInt(AccountStock::getQuantity)
                            .sum();

                    BigDecimal totalInvested = positions.stream()
                            .map(p -> p.getAveragePrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal weightedAvgPrice = totalQuantity > 0
                            ? totalInvested.divide(BigDecimal.valueOf(totalQuantity), 4, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return new AccountStockResponseDto(
                            stock.getStockId(),
                            stock.getDescription(),
                            stock.getDescription(),
                            totalQuantity,
                            weightedAvgPrice.doubleValue(),
                            totalInvested.doubleValue(),
                            "https://icons.brapi.dev/icons/" + stock.getStockId() + ".svg"
                    );
                })
                .toList();
    }

}
