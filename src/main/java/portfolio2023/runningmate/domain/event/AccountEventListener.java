package portfolio2023.runningmate.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.mail.EmailMessage;
import portfolio2023.runningmate.mail.EmailService;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.security.config.AppProperties;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class AccountEventListener {

    private final AccountRepository accountRepository;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    @EventListener
    public void handleCheckEmailEvent(CheckEmailEvent checkEmailEvent){
        Account account = checkEmailEvent.getAccount();
        sendCheckEmail(account);
    }

    private void sendCheckEmail(Account account) {
        Context context = new Context();
        context.setVariable("link", "/running-mate/check-email-token?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "러닝 메이트 서비스를 이용하시려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("러닝 메이트, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
