package portfolio2023.runningmate.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;
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
    private String fileName;
    private String filePath;
    private String profileImage;
}
