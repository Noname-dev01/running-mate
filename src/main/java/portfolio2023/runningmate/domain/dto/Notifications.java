package portfolio2023.runningmate.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import portfolio2023.runningmate.domain.Account;

@Data
public class Notifications {

    private boolean CrewCreatedByEmail;

    private boolean CrewCreatedByWeb;

    private boolean CrewRecruitByEmail;

    private boolean CrewRecruitByWeb;

    private boolean CrewUpdatedByEmail;

    private boolean CrewUpdatedByWeb;

}
