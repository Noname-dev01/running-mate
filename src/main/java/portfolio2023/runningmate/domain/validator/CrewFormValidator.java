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
        if (crewRepository.existsByTitle(crewForm.getTitle())){
            errors.rejectValue("title", "wrong.title", "해당 크루 이름은 사용중이거나 형식이 올바르지 않습니다.");
        }
    }
}
