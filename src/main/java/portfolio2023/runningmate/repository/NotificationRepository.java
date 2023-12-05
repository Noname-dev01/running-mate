package portfolio2023.runningmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import portfolio2023.runningmate.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
