package github.maxsuel.agregadordeinvestimentos.repository;

import github.maxsuel.agregadordeinvestimentos.entity.AccountStock;
import github.maxsuel.agregadordeinvestimentos.entity.AccountStockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountStockRepository extends JpaRepository<AccountStock, AccountStockId> {
}
