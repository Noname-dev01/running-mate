package portfolio2023.runningmate.domain.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class NicknameForm {

    @NotBlank
    @Length(min = 3, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$",
            message = "닉네임은 3 ~ 20자로 설정해주세요. 공백 또는 특수문자는 사용할 수 없습니다.")
    private String nickname;
}
