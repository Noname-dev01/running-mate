package portfolio2023.runningmate.domain.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import portfolio2023.runningmate.domain.dto.CrewForm;
import portfolio2023.runningmate.repository.CrewRepository;

@Component
@RequiredArgsConstructor
public class CrewFormValidator implements Validator {

    private final CrewRepository crewRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CrewForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CrewForm crewForm = (CrewForm) target;
        if (crewRepository.existsByPath(crewForm.getPath())){
            errors.rejectValue("path", "wrong.path", "해당 크루 경로값을 사용할 수 없습니다.");
        }
    }
}
