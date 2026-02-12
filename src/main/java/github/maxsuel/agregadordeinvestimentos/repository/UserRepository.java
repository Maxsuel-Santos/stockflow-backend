package github.maxsuel.agregadordeinvestimentos.repository;

import java.util.Optional;
import java.util.UUID;

import github.maxsuel.agregadordeinvestimentos.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
