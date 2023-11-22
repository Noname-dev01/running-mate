package portfolio2023.runningmate.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import portfolio2023.runningmate.domain.Crew;

public interface CrewRepository extends JpaRepository<Crew,Long> {
    boolean existsByTitle(String title);

    @EntityGraph(attributePaths = {"tags","zones", "manager", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Crew findByTitle(String title);

    @EntityGraph(attributePaths = "members")
    Crew findCrewWithMembersByTitle(String title);

    @EntityGraph(attributePaths = {"tags", "manager"})
    Crew findCrewWithTagsByTitle(String title);

    @EntityGraph(attributePaths = {"zones", "manager"})
    Crew findCrewWithZonesByTitle(String title);
}