package github.maxsuel.agregadordeinvestimentos.repository;

import github.maxsuel.agregadordeinvestimentos.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {
}
