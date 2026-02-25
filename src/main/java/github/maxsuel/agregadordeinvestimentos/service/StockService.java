package github.maxsuel.agregadordeinvestimentos.service;

import github.maxsuel.agregadordeinvestimentos.client.BrapiClient;
import github.maxsuel.agregadordeinvestimentos.dto.request.stock.CreateStockDto;
import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountStockResponseDto;
import github.maxsuel.agregadordeinvestimentos.entity.AccountStock;
import github.maxsuel.agregadordeinvestimentos.entity.User;
import github.maxsuel.agregadordeinvestimentos.exceptions.UserNotFoundException;
import github.maxsuel.agregadordeinvestimentos.mapper.StockMapper;
import github.maxsuel.agregadordeinvestimentos.repository.StockRepository;
import github.maxsuel.agregadordeinvestimentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final StockMapper stockMapper;
    private final BrapiClient  brapiClient;

    @Value("${BRAPI_TOKEN}")
    private String TOKEN; // Brapi API token

    @Transactional
    public void createStock(@NonNull CreateStockDto createStockDto) {
        var stock = stockMapper.toEntity(createStockDto);

        stockRepository.save(stock);
    }

    @Transactional(readOnly = true)
    public List<AccountStockResponseDto> listOwnedStocks(@NotNull User user) {
        var userWithAccounts = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return userWithAccounts.getAccounts().stream()
                .flatMap(acc -> acc.getAccountStocks().stream())
                .collect(Collectors.groupingBy(AccountStock::getStock))
                .entrySet().stream()
                .map(entry -> {
                    var stock = entry.getKey();
                    var positions = entry.getValue();

                    int totalQty = positions.stream().mapToInt(AccountStock::getQuantity).sum();
                    BigDecimal totalInvested = positions.stream()
                            .map(p -> p.getAveragePrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    double avgPrice = totalQty > 0
                            ? totalInvested.divide(BigDecimal.valueOf(totalQty), 2, RoundingMode.HALF_UP).doubleValue()
                            : 0.0;

                    try {
                        var response = brapiClient.getQuote(TOKEN, stock.getStockId(), "summaryProfile");
                        var market = response.results().getFirst();

                        String sector = "N/A";

                        if (market.summaryProfile() != null && market.summaryProfile().sector() != null) {
                            sector = market.summaryProfile().sector();
                        } else if (stock.getSector() != null) {
                            sector = stock.getSector();
                        }

                        return new AccountStockResponseDto(
                                stock.getStockId(),
                                market.shortName(),
                                market.longName(),
                                sector,
                                totalQty,
                                avgPrice,
                                market.regularMarketPrice(),
                                market.regularMarketChangePercent(),
                                market.regularMarketVolume(),
                                totalQty * market.regularMarketPrice(),
                                totalInvested.doubleValue(),
                                market.logourl()
                        );
                    } catch (Exception e) {
                        log.error("Failed to fetch Brapi data for {}: {}", stock.getStockId(), e.getMessage());
                        return new AccountStockResponseDto(
                                stock.getStockId(),
                                stock.getName(),
                                stock.getLongName(),
                                stock.getSector() != null ? stock.getSector() : "N/A",
                                totalQty,
                                avgPrice,
                                0.0, 0.0, 0L, 0.0,
                                totalInvested.doubleValue(),
                                stock.getLogoUrl()
                        );
                    }
                }).toList();
    }

}
