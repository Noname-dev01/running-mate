package portfolio2023.runningmate.domain.event;

import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import portfolio2023.runningmate.domain.Crew;

@Getter
public class CrewCreatedEvent {

    private Crew crew;

    public CrewCreatedEvent(Crew crew) {
        this.crew = crew;
    }
}
