package andreea.poc.db.locking.model;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByIban(String iban);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select ba from BankAccount ba where ba.id in :ids order by ba.id asc")
    List<BankAccount> findOrderedAccountsByIdInForUpdate(@Param("ids") Set<Long> ids);
}
