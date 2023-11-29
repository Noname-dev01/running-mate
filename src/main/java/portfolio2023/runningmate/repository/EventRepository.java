package portfolio2023.runningmate.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @EntityGraph(attributePaths = "enrollments", type = EntityGraph.EntityGraphType.LOAD)
    List<Event> findByCrewOrderByStartDateTime(Crew crew);

}
