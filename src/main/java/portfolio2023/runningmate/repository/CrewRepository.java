package portfolio2023.runningmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import portfolio2023.runningmate.domain.Crew;

public interface CrewRepository extends JpaRepository<Crew,Long> {
    boolean existsByPath(String path);
}
