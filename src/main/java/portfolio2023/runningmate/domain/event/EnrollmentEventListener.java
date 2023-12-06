package portfolio2023.runningmate.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import portfolio2023.runningmate.domain.*;
import portfolio2023.runningmate.mail.EmailMessage;
import portfolio2023.runningmate.mail.EmailService;
import portfolio2023.runningmate.repository.NotificationRepository;
import portfolio2023.runningmate.security.config.AppProperties;

import java.time.LocalDateTime;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final NotificationRepository notificationRepository;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    @EventListener
    public void handleEnrollmentEvent(EnrollmentEvent enrollmentEvent){
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Event event = enrollment.getEvent();
        Crew crew = event.getCrew();

        if (account.isCrewRecruitByEmail()){
            sendEmail(enrollmentEvent, account, event, crew);
        }

        if (account.isCrewRecruitByWeb()){
            sendNotification(enrollmentEvent, account, event, crew);
        }
    }

    private void sendEmail(EnrollmentEvent enrollmentEvent, Account account, Event event, Crew crew) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/running-mate/crew/"+ crew.getEncodedTitle()+"/events/"+event.getId());
        context.setVariable("linkName", crew.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("러닝 메이트, " + event.getTitle() + " 모임 참가 신청 결과입니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    private void sendNotification(EnrollmentEvent enrollmentEvent, Account account, Event event, Crew crew) {
        Notification notification = new Notification();
        notification.setTitle(crew.getTitle() + " / " + event.getTitle());
        notification.setLink("/running-mate/crew/"+ crew.getEncodedTitle() + "/events/"+ event.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(enrollmentEvent.getMessage());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.EVENT_ENROLLMENT);
        notificationRepository.save(notification);
    }
}
