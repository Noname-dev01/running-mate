package portfolio2023.runningmate.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.Tag;
import portfolio2023.runningmate.domain.Zone;

import java.util.List;
import java.util.Set;

public interface CrewRepositoryCustom {

    Page<Crew> findByKeyword(String keyword, Pageable pageable);

    List<Crew> findByAccountCrewList(Set<Tag> tags, Set<Zone> zones);
}
