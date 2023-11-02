package portfolio2023.runningmate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.repository.AccountRepository;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;
    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("회원 가입 화면 뷰 테스트")
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/running-mate/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
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
                .andExpect(view().name("account/sign-up"));
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 정상")
    public void signUp_with_correct_input() throws Exception {
        mockMvc.perform(post("/running-mate/sign-up")
                        .param("nickname","admin")
                        .param("email", "admin@email.com")
                        .param("password","123456789")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/running-mate"));

        Account account = accountRepository.findByEmail("admin@email.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "123456789");
        assertNotNull(account.getEmailCheckToken());
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 오류")
    public void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/running-mate/check-email-token")
                .param("token","sdfsdfsdfsdf")
                .param("email","email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"));
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
                .andExpect(view().name("account/checked-email"));
    }
}