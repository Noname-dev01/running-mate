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
import java.util.HashSet;
import java.util.Set;

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
                sendCrewCreatedEmail(account, crew, "새로운 크루가 생겼습니다.",
                        "러닝 메이트, '" + crew.getTitle() + "' 크루가 생겼습니다.");
            }

            if (account.isCrewCreatedByWeb()){
                sendCrewNotification(account, crew, crew.getShortDescription(), NotificationType.CREW_CREATED);
            }
        });
    }

    @EventListener
    public void handleCrewUpdateEvent(CrewUpdateEvent crewUpdateEvent){
        Crew crew = crewRepository.findCrewWithManagersAndMembersById(crewUpdateEvent.getCrew().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(crew.getMembers());
        accounts.add(crew.getManager());

        accounts.forEach(account -> {
            if (account.isCrewUpdatedByEmail()){
                sendCrewCreatedEmail(account, crew, crewUpdateEvent.getMessage(),
                        "러닝 메이트, '" + crew.getTitle() + "' 크루에 새소식이 있습니다.");
            }

            if (account.isCrewUpdatedByWeb()){
                sendCrewNotification(account, crew, crewUpdateEvent.getMessage(), NotificationType.CREW_UPDATED);
            }
        });
    }

    private void sendCrewNotification(Account account, Crew crew, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(crew.getTitle());
        notification.setLink("/running-mate/crew/"+ crew.getEncodedTitle());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendCrewCreatedEmail(Account account, Crew crew, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/running-mate/crew/"+ crew.getEncodedTitle());
        context.setVariable("linkName", crew.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
