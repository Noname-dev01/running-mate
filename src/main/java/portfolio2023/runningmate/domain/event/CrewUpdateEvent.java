package portfolio2023.runningmate.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import portfolio2023.runningmate.domain.Crew;

@Getter
@RequiredArgsConstructor
public class CrewUpdateEvent {

    private final Crew crew;

    private final String message;
}
