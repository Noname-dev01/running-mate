package portfolio2023.runningmate.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import portfolio2023.runningmate.domain.Account;

@Data
public class Notifications {

    private boolean runningCreatedByEmail;

    private boolean runningCreatedByWeb;

    private boolean runningRecruitByEmail;

    private boolean runningRecruitByWeb;

    private boolean runningUpdatedByEmail;

    private boolean runningUpdatedByWeb;

}
