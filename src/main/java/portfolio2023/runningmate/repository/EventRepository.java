package portfolio2023.runningmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import portfolio2023.runningmate.domain.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
