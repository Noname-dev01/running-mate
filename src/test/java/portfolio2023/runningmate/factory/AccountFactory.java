package portfolio2023.runningmate.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.repository.AccountRepository;

@Component
public class AccountFactory {

    @Autowired AccountRepository accountRepository;

    public Account createAccount(String nickname){
        Account account = new Account();
        account.setNickname(nickname);
        account.setEmail(nickname+"@email.com");
        accountRepository.save(account);
        return account;
    }
}
