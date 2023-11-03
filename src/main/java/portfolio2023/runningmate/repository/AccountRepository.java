package portfolio2023.runningmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import portfolio2023.runningmate.domain.Account;

public interface AccountRepository extends JpaRepository<Account,Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);

    Account findByNickname(String nickname);
}
