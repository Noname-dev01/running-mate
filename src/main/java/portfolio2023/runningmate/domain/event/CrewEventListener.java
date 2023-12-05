package portfolio2023.runningmate.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.Notification;
import portfolio2023.runningmate.domain.NotificationType;
import portfolio2023.runningmate.mail.EmailMessage;
import portfolio2023.runningmate.mail.EmailService;
import portfolio2023.runningmate.repository.AccountPredicates;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.repository.CrewRepository;
import portfolio2023.runningmate.repository.NotificationRepository;
import portfolio2023.runningmate.security.config.AppProperties;

import java.time.LocalDateTime;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class CrewEventListener {

    private final CrewRepository crewRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleCrewCreatedEvent(CrewCreatedEvent crewCreatedEvent){
        Crew crew = crewRepository.findCrewWithTagsAndZonesById(crewCreatedEvent.getCrew().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(crew.getTags(), crew.getZones()));
        accounts.forEach(account -> {
            if (account.isCrewCreatedByEmail()){
                sendCrewCreatedEmail(account, crew);
            }

            if (account.isCrewCreatedByWeb()){
                sendCrewCreatedNoti(account, crew);
            }
        });
    }

    private void sendCrewCreatedNoti(Account account, Crew crew) {
        Notification notification = new Notification();
        notification.setTitle(crew.getTitle());
        notification.setLink("/running-mate/crew/"+ crew.getEncodedTitle());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(crew.getShortDescription());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.CREW_CREATED);
        notificationRepository.save(notification);
    }

    private void sendCrewCreatedEmail(Account account, Crew crew) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/running-mate/crew/"+ crew.getEncodedTitle());
        context.setVariable("linkName", crew.getTitle());
        context.setVariable("message", "새로운 크루가 생겼습니다.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("러닝 메이트, '" + crew.getTitle() + "' 크루가 생겼습니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
