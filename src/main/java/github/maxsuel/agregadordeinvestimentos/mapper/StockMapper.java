package github.maxsuel.agregadordeinvestimentos.mapper;

import github.maxsuel.agregadordeinvestimentos.dto.request.stock.CreateStockDto;
import github.maxsuel.agregadordeinvestimentos.entity.Stock;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StockMapper {

    Stock toEntity(CreateStockDto createStockDto);

}
