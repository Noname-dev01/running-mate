package portfolio2023.runningmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Notification;
import portfolio2023.runningmate.security.CurrentAccount;
import portfolio2023.runningmate.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/running-mate")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String getNotifications(@CurrentAccount Account account, Model model){
        List<Notification> notifications = notificationService.findByAccountNotification(account);
        long numberOfChecked = notificationService.numberOfChecked(account);
        putCategorizedNotifications(model, notifications, numberOfChecked, notifications.size());
        model.addAttribute("isNew", true);
        notificationService.markAsRead(notifications);
        return "notification/list";
    }

    @GetMapping("/notifications/old")
    public String getOldNotifications(@CurrentAccount Account account, Model model){
        List<Notification> notifications = notificationService.findByAccountOldNotification(account);
        long numberOfNotChecked = notificationService.numberOfNotChecked(account);
        putCategorizedNotifications(model, notifications, notifications.size(), numberOfNotChecked);
        model.addAttribute("isNew", false);
        return "notification/list";
    }

    @PostMapping("/notifications")
    public String deleteNotifications(@CurrentAccount Account account){
        notificationService.deleteNotiChecked(account, true);
        return "redirect:/running-mate/notifications";
    }

    private void putCategorizedNotifications(Model model, List<Notification> notifications, long numberOfChecked, long numberOfNotChecked) {
        List<Notification> newCrewNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> watchingCrewNotifications = new ArrayList<>();

        for (var notification : notifications){
            switch (notification.getNotificationType()){
                case CREW_CREATED: newCrewNotifications.add(notification); break;
                case EVENT_ENROLLMENT: eventEnrollmentNotifications.add(notification); break;
                case CREW_UPDATED: watchingCrewNotifications.add(notification); break;
            }
        }

        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newCrewNotifications", newCrewNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingCrewNotifications", watchingCrewNotifications);
    }
}
