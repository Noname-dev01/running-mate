package portfolio2023.runningmate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Notification;
import portfolio2023.runningmate.repository.NotificationRepository;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> findByAccountNotification(Account account) {
        return notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, false);
    }

    public List<Notification> findByAccountOldNotification(Account account) {
        return notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, true);
    }

    public long numberOfChecked(Account account) {
        return notificationRepository.countByAccountAndChecked(account, true);
    }

    public long numberOfNotChecked(Account account) {
        return notificationRepository.countByAccountAndChecked(account, false);
    }

    public void markAsRead(List<Notification> notifications) {
        notifications.forEach(notification -> notification.setChecked(true));
        notificationRepository.saveAll(notifications);
    }

    public void deleteNotiChecked(Account account, boolean checked) {
        notificationRepository.deleteByAccountAndChecked(account, checked);
    }
}
