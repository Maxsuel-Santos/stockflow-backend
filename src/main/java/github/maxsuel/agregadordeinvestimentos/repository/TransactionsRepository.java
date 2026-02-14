package github.maxsuel.agregadordeinvestimentos.repository;

import github.maxsuel.agregadordeinvestimentos.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, UUID> {

    List<Transactions> findAllByUser_UserId(UUID userId);

}
