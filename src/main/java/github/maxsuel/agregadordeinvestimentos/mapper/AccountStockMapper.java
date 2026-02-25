package github.maxsuel.agregadordeinvestimentos.mapper;

import github.maxsuel.agregadordeinvestimentos.dto.response.account.AccountStockResponseDto;
import github.maxsuel.agregadordeinvestimentos.dto.external.brapi.StockDto;
import github.maxsuel.agregadordeinvestimentos.entity.AccountStock;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface AccountStockMapper {

    @Mapping(target = "stockId", source = "stockDto.stock")
    @Mapping(target = "name", source = "stockDto.shortName")
    @Mapping(target = "longName", source = "stockDto.longName")
    @Mapping(target = "sector", expression = "java(mapSector(accountStock, stockDto))")
    @Mapping(target = "quantity", source = "accountStock.quantity")
    @Mapping(target = "currentPrice", source = "stockDto.regularMarketPrice")
    @Mapping(target = "avgPrice", source = "accountStock.averagePrice")
    @Mapping(target = "change", source = "stockDto.regularMarketChangePercent")
    @Mapping(target = "volume", source = "stockDto.regularMarketVolume")
    @Mapping(target = "marketValue", expression = "java(calculateTotal(accountStock.getQuantity(), stockDto.regularMarketPrice()))")
    @Mapping(target = "total", expression = "java(calculateTotal(accountStock.getQuantity(), accountStock.getAveragePrice().doubleValue()))")
    @Mapping(target = "logoUrl", source = "stockDto.logourl")
    AccountStockResponseDto toDto(AccountStock accountStock, StockDto stockDto);

    default String mapSector(AccountStock accountStock, @NotNull StockDto stockDto) {
        if (stockDto.summaryProfile() != null && stockDto.summaryProfile().sector() != null) {
            return stockDto.summaryProfile().sector();
        }
        return (accountStock.getStock() != null && accountStock.getStock().getSector() != null)
                ? accountStock.getStock().getSector()
                : "N/A";
    }

    default double calculateTotal(double quantity, double price) {
        return BigDecimal.valueOf(quantity * price)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

}
