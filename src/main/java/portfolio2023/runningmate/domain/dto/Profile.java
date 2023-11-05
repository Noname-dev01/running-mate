package portfolio2023.runningmate.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import portfolio2023.runningmate.domain.Account;

@Data
public class Profile {

    @Length(max = 35)
    private String introduction;
    @Length(max = 50)
    private String url;
    @Length(max = 50)
    private String occupation;
    @Length(max = 50)
    private String location;

    public Profile() {
    }

    public Profile(Account account) {
        this.introduction = account.getIntroduction();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
    }
}
