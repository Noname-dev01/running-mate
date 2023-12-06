package portfolio2023.runningmate.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import portfolio2023.runningmate.domain.Enrollment;

@Getter
@RequiredArgsConstructor
public abstract class EnrollmentEvent {

    protected final Enrollment enrollment;

    protected final String message;

}
