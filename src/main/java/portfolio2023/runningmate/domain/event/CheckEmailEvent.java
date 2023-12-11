package portfolio2023.runningmate.domain.event;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import portfolio2023.runningmate.domain.Account;

@Getter
@RequiredArgsConstructor
public class CheckEmailEvent {

    private final Account account;

}
