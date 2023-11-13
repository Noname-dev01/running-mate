package portfolio2023.runningmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import portfolio2023.runningmate.domain.Zone;

public interface ZoneRepository extends JpaRepository<Zone,Long> {

    Zone findByCityAndProvince(String cityName, String provinceName);

}
