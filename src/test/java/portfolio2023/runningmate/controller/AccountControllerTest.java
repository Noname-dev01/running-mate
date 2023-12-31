package portfolio2023.runningmate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.mail.EmailMessage;
import portfolio2023.runningmate.mail.EmailService;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.service.AccountService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @MockBean EmailService emailService;
    @Autowired AccountService accountService;

    @Test
    @DisplayName("회원 가입 화면 뷰 테스트")
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/running-mate/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 오류")
    public void signUp_with_wrong_input() throws Exception {
        mockMvc.perform(post("/running-mate/sign-up")
                .param("nickname","admin")
                .param("email", "email...")
                .param("password","12345")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 정상")
    public void signUp_with_correct_input() throws Exception {
        accountRepository.deleteAll();

        mockMvc.perform(post("/running-mate/sign-up")
                        .param("nickname","test")
                        .param("email", "test@email.com")
                        .param("password","123456789")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/running-mate"))
                .andExpect(authenticated());

        Account account = accountRepository.findByEmail("test@email.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "123456789");
        assertNotNull(account.getEmailCheckToken());
    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 오류")
    public void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/running-mate/check-email-token")
                .param("token","sdfsdfsdfsdf")
                .param("email","email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 정상")
    public void checkEmailToken() throws Exception {

        Account account = Account.builder()
                .email("email@email.com")
                .password("123456789")
                .nickname("email")
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/running-mate/check-email-token")
                        .param("token",newAccount.getEmailCheckToken())
                        .param("email",newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated());
    }

    @Test
    @DisplayName("로그인_이메일 사용")
    public void login_with_email() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("admin");
        signUpForm.setEmail("admin@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);

        mockMvc.perform(post("/running-mate/login")
                .param("username", "admin@email.com")
                .param("password","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate"))
                .andExpect(authenticated().withUsername("admin"));

    }

    @Test
    @DisplayName("로그인_닉네임 사용")
    public void login_with_nickname() throws Exception {

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("admin");
        signUpForm.setEmail("admin@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);

        mockMvc.perform(post("/running-mate/login")
                        .param("username", "admin")
                        .param("password","12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate"))
                .andExpect(authenticated().withUsername("admin"));

    }

    @Test
    @DisplayName("로그인 실패")
    public void login_fail() throws Exception {
        mockMvc.perform(post("/running-mate/login")
                .param("username","fail")
                .param("password", "fail")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @WithMockUser
    @DisplayName("로그아웃")
    public void logout() throws Exception{
        mockMvc.perform(post("/running-mate/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("프로필 뷰 테스트")
    public void profileView() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("admin");
        signUpForm.setEmail("admin@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
        Account account = accountService.findByNickname("admin");
        accountService.login(account);

        mockMvc.perform(get("/running-mate/profile/admin"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("isOwner"))
                .andExpect(view().name("account/profile"));
    }

    @Test
    @DisplayName("패스워드 기억나지 않을 경우 - 이메일 로그인")
    public void email_login_form() throws Exception{
        mockMvc.perform(get("/running-mate/email-login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("패스워드 기억나지 않을 경우 - 로그인 링크 전송 성공")
    public void email_login_success() throws Exception{
        Account account = new Account();
        account.setEmail("admin@email.com");
        account.setNickname("admin");
        account.setPassword("12345678");
        account.generateEmailCheckToken();
        accountRepository.save(account);

        Account account1 = accountService.findByEmail("admin@email.com");
        account1.setEmailCheckTokenGeneratedAt(LocalDateTime.now().minusMinutes(5));

        mockMvc.perform(post("/running-mate/email-login")
                        .param("email", "admin@email.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"));

        assertNotNull(account.getEmailCheckToken());
        then(emailService).should().sendEmail(any(EmailMessage.class));
    }

    @Test
    @DisplayName("패스워드 기억나지 않을 경우 - 로그인 링크 전송 실패(5분 마다 전송 가능)")
    public void email_login_fail_5min() throws Exception{
        mockMvc.perform(post("/running-mate/email-login")
                        .param("email", "admin@email.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));

    }

    @Test
    @DisplayName("패스워드 기억나지 않을 경우 - 로그인 링크 전송 실패(유효하지 않은 이메일)")
    public void email_login_fail_wrong_email() throws Exception{
        mockMvc.perform(post("/running-mate/email-login")
                        .param("email", "admin1@email.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

}