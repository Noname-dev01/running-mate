package portfolio2023.runningmate.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.service.AccountService;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    AccountService accountService;
    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeEach(){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("admin");
        signUpForm.setEmail("admin@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach()
    void afterEach(){
        accountRepository.deleteAll();
    }


    @Test
    @DisplayName("프로필 수정 폼")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateProfileForm() throws Exception {
        mockMvc.perform(get("/running-mate/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }
    @Test
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateProfile() throws Exception {
        String introduction = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post("/running-mate/settings/profile")
                        .param("introduction", introduction)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/settings/profile"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("admin");
        assertEquals(introduction, account.getIntroduction());
    }
    @Test
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateProfile_error() throws Exception {
        String introduction = "길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 너무나도 길게 소개를 수정하는 경우. ";
        mockMvc.perform(post("/running-mate/settings/profile")
                        .param("introduction", introduction)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/update-profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("admin");
        assertNull(account.getIntroduction());
    }

    @Test
    @DisplayName("패스워드 수정 폼")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updatePassword_form() throws Exception {
        mockMvc.perform(get("/running-mate/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @Test
    @DisplayName("패스워드 수정 - 정상")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updatePassword_success() throws Exception {
        mockMvc.perform(post("/running-mate/settings/password")
                .param("newPassword", "12345678")
                .param("newPasswordCheck","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/settings/password"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountService.findByNickname("admin");
        assertTrue(passwordEncoder.matches("12345678",account.getPassword()));
    }

    @Test
    @DisplayName("패스워드 수정 - 입력값 에러 - 패스워드확인 불일치")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updatePassword_fail() throws Exception {
        mockMvc.perform(post("/running-mate/settings/password")
                .param("newPassword","12345678")
                .param("newPasswordCheck","111111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/update-password"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }
}