package portfolio2023.runningmate.domain.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import portfolio2023.runningmate.domain.Crew;

@Slf4j
@Async
@Component
@Transactional(readOnly = true)
public class CrewEventListener {

    @EventListener
    public void handleCrewCreatedEvent(CrewCreatedEvent crewCreatedEvent){
        Crew crew = crewCreatedEvent.getCrew();
        log.info(crew.getTitle()+ "is created");
    }
}
