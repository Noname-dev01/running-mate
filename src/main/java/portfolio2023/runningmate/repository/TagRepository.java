package portfolio2023.runningmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import portfolio2023.runningmate.domain.Tag;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {


    Tag findByTitle(String title);
}
