package portfolio2023.runningmate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import portfolio2023.runningmate.converter.Base64ToMultipartFile;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Tag;
import portfolio2023.runningmate.domain.Zone;
import portfolio2023.runningmate.domain.dto.Notifications;
import portfolio2023.runningmate.domain.dto.Profile;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.domain.event.CheckEmailEvent;
import portfolio2023.runningmate.mail.EmailMessage;
import portfolio2023.runningmate.mail.EmailService;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.s3.S3Uploader;
import portfolio2023.runningmate.security.UserAccount;
import portfolio2023.runningmate.security.config.AppProperties;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final S3Uploader s3Uploader;
    private final Base64ToMultipartFile base64ToMultipartFile;

    public Account processNewAccount(SignUpForm signUpForm){
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        eventPublisher.publishEvent(new CheckEmailEvent(newAccount));
    }
    
    public Account findByEmail(String email){
        return accountRepository.findByEmail(email);
    }

    public long numberOfUser(){
         return accountRepository.count();
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if (account == null){
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null){
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account);
    }

    @Transactional(readOnly = true)
    public Account findByNickname(String nickname) {
        return accountRepository.findByNickname(nickname);
    }

    public void updateProfile(Account account, Profile profile) throws IOException {
        if (profile.getProfileImage() != null && !profile.getProfileImage().isEmpty()){
            MultipartFile file = base64ToMultipartFile.convert(profile.getProfileImage(), profile.getFileName());
            if (file != null && !file.isEmpty()){
                String storedFileName = s3Uploader.upload(file, "images/profile");
                account.setFilePath(storedFileName);
                account.setFileName(file.getOriginalFilename());
            }
        }
        account.setIntroduction(profile.getIntroduction());
        account.setUrl(profile.getUrl());
        account.setOccupation(profile.getOccupation());
        account.setLocation(profile.getLocation());
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account);
    }

    public void sendLoginLink(Account account) {
        Context context = new Context();
        context.setVariable("link", "/running-mate/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "러닝 메이트 로그인 하기");
        context.setVariable("message", "로그인 하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("러닝 메이트, 로그인 링크")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }

    public Account getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        if (account == null){
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }
        return account;
    }

    public Account accountLoaded(Account account) {
       return accountRepository.findAccountWithTagsAndZonesById(account.getId());
    }
}
