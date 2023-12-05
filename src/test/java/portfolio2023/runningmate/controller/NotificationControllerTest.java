package portfolio2023.runningmate.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import portfolio2023.runningmate.domain.*;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.factory.CrewFactory;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.service.AccountService;
import portfolio2023.runningmate.service.NotificationService;

import javax.transaction.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired NotificationService notificationService;
    @Autowired CrewFactory crewFactory;
    @Autowired ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("admin");
        signUpForm.setEmail("admin@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("알림 목록 조회 - 읽지 않은 알림")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void getNotifications() throws Exception {

        mockMvc.perform(get("/running-mate/notifications")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("isNew"))
                .andExpect(model().attributeExists("numberOfNotChecked"))
                .andExpect(model().attributeExists("numberOfChecked"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attributeExists("newCrewNotifications"))
                .andExpect(model().attributeExists("eventEnrollmentNotifications"))
                .andExpect(model().attributeExists("watchingCrewNotifications"))
                .andExpect(view().name("notification/list"));
    }


}