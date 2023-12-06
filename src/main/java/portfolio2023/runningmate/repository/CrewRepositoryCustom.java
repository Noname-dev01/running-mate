package portfolio2023.runningmate.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import portfolio2023.runningmate.domain.Crew;

import java.util.List;

public interface CrewRepositoryCustom {

    Page<Crew> findByKeyword(String keyword, Pageable pageable);
}
