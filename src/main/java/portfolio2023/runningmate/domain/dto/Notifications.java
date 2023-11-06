package portfolio2023.runningmate.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import portfolio2023.runningmate.domain.Account;

@Data
@NoArgsConstructor
public class Notifications {

    private boolean runningCreatedByEmail;

    private boolean runningCreatedByWeb;

    private boolean runningRecruitByEmail;

    private boolean runningRecruitByWeb;

    private boolean runningUpdatedByEmail;

    private boolean runningUpdatedByWeb;

    public Notifications(Account account) {
        this.runningCreatedByEmail = account.isRunningCreatedByEmail();
        this.runningCreatedByWeb = account.isRunningCreatedByWeb();
        this.runningRecruitByEmail = account.isRunningRecruitByEmail();
        this.runningRecruitByWeb = account.isRunningRecruitByWeb();
        this.runningUpdatedByEmail = account.isRunningUpdatedByEmail();
        this.runningUpdatedByWeb = account.isRunningUpdatedByWeb();
    }
}
