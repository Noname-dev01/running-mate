package portfolio2023.runningmate.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.Tag;
import portfolio2023.runningmate.domain.Zone;

import java.util.List;
import java.util.Set;

public interface CrewRepository extends JpaRepository<Crew,Long>, CrewRepositoryCustom {
    boolean existsByTitle(String title);

    @EntityGraph(attributePaths = {"tags","zones", "manager", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Crew findByTitle(String title);

    @EntityGraph(attributePaths = "members")
    Crew findCrewWithMembersByTitle(String title);

    @EntityGraph(attributePaths = {"tags", "manager"})
    Crew findCrewWithTagsByTitle(String title);

    @EntityGraph(attributePaths = {"zones", "manager"})
    Crew findCrewWithZonesByTitle(String title);

    @EntityGraph(attributePaths = "manager")
    Crew findCrewWithManagerByTitle(String title);

    Crew findCrewOnlyByTitle(String title);

    @EntityGraph(attributePaths = {"tags", "zones"}, type = EntityGraph.EntityGraphType.FETCH)
    Crew findCrewWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"members", "manager"})
    Crew findCrewWithManagersAndMembersById(Long id);

    @EntityGraph(attributePaths = {"zones", "tags"})
    List<Crew> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Crew> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Crew> findFirst5ByManagerContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}
