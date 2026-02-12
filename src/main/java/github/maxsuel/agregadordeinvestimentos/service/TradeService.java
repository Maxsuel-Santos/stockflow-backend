package github.maxsuel.agregadordeinvestimentos.service;

import github.maxsuel.agregadordeinvestimentos.client.BrapiClient;
import github.maxsuel.agregadordeinvestimentos.dto.request.stock.TradeRequestDto;
import github.maxsuel.agregadordeinvestimentos.entity.*;
import github.maxsuel.agregadordeinvestimentos.entity.enums.TradeType;
import github.maxsuel.agregadordeinvestimentos.exceptions.AccountNotFoundException;
import github.maxsuel.agregadordeinvestimentos.exceptions.InsufficientFundsException;
import github.maxsuel.agregadordeinvestimentos.exceptions.InsufficientSharesException;
import github.maxsuel.agregadordeinvestimentos.exceptions.StockNotFoundException;
import github.maxsuel.agregadordeinvestimentos.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final BrapiClient brapiClient;
    private final UserRepository userRepository;
    private final AccountStockRepository accountStockRepository;
    private final TransactionsRepository transactionsRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;

    @Value("${BRAPI_TOKEN}")
    private String TOKEN;

    @Transactional
    public void executeBuy(@NonNull User user, @NonNull TradeRequestDto dto) {
        var response = brapiClient.getQuote(TOKEN, dto.stockId());

        BigDecimal currentPrice = BigDecimal.valueOf(response.results().getFirst().regularMarketPrice());
        BigDecimal totalCost = currentPrice.multiply(BigDecimal.valueOf(dto.quantity()));

        BigDecimal userCash = (user.getCash() != null) ? user.getCash() : BigDecimal.ZERO;

        if (userCash.compareTo(totalCost) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Current balance: $ " + userCash);
        }

        var account = accountRepository.findById(dto.accountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found."));
        var stock = stockRepository.findById(dto.stockId())
                .orElseThrow(() -> new StockNotFoundException("Stock ticker not recognized."));

        var id = new AccountStockId(dto.accountId(), dto.stockId());
        AccountStock accountStock = accountStockRepository.findById(id)
                .orElse(new AccountStock(id, account, stock, 0, BigDecimal.ZERO));

        BigDecimal currentTotalValue = accountStock.getAveragePrice().multiply(BigDecimal.valueOf(accountStock.getQuantity()));
        BigDecimal newTotalValue = currentTotalValue.add(totalCost);
        int totalQuantity = accountStock.getQuantity() + dto.quantity();

        BigDecimal newAveragePrice = newTotalValue.divide(BigDecimal.valueOf(totalQuantity), 4, RoundingMode.HALF_UP);

        accountStock.setQuantity(totalQuantity);
        accountStock.setAveragePrice(newAveragePrice);
        accountStockRepository.save(accountStock);

        user.setCash(userCash.subtract(totalCost));
        userRepository.save(user);

        saveLog(user, account, stock, dto.quantity(), currentPrice, TradeType.BUY);
    }

    @Transactional
    public void executeSell(@NonNull User user, @NonNull TradeRequestDto dto) {
        var id = new AccountStockId(dto.accountId(), dto.stockId());
        var accountStock = accountStockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException("You do not own this stock in this portfolio."));

        if (accountStock.getQuantity() < dto.quantity()) {
            throw new InsufficientSharesException("Insufficient quantity for sale.");
        }

        var response = brapiClient.getQuote(TOKEN, dto.stockId());
        BigDecimal currentPrice = BigDecimal.valueOf(response.results().getFirst().regularMarketPrice());
        BigDecimal totalReceive = currentPrice.multiply(BigDecimal.valueOf(dto.quantity()));

        BigDecimal currentCash = (user.getCash() != null) ? user.getCash() : BigDecimal.ZERO;

        accountStock.setQuantity(accountStock.getQuantity() - dto.quantity());

        if (accountStock.getQuantity() == 0) {
            accountStockRepository.delete(accountStock);
        } else {
            accountStockRepository.save(accountStock);
        }

        user.setCash(currentCash.add(totalReceive));
        userRepository.save(user);

        saveLog(user, accountStock.getAccount(), accountStock.getStock(), dto.quantity(), currentPrice, TradeType.SELL);
    }

    private void saveLog(User user, Account acc, Stock stock, Integer qty, BigDecimal price, TradeType type) {
        var tx = Transactions.builder()
                .user(user)
                .account(acc)
                .stock(stock)
                .quantity(qty)
                .priceAtTime(price)
                .type(type)
                .build();

        transactionsRepository.save(tx);
    }

}
