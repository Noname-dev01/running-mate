package portfolio2023.runningmate.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.EventType;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.repository.CrewRepository;
import portfolio2023.runningmate.service.AccountService;
import portfolio2023.runningmate.service.CrewService;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EventControllerTest {

    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired CrewService crewService;
    @Autowired CrewRepository crewRepository;
    @Autowired MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("admin");
        signUpForm.setEmail("admin@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);

        Account account = accountRepository.findByNickname("admin");

        Crew crew = new Crew();
        crew.setTitle("test");
        crew.setShortDescription("short description");
        crew.setFullDescription("full description");
        crewService.createNewCrew(crew, account);
    }

    @AfterEach
    void afterEach() {
        crewRepository.deleteAll();
        accountRepository.deleteAll();
    }


    @Test
    @DisplayName("모임 만들기 뷰")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void newEventForm() throws Exception {
        Crew crew = crewService.findByTitle("test");

        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/new-event")
                .with(csrf()))
                .andExpect(model().attributeExists("crew"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("eventForm"))
                .andExpect(view().name("event/form"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("모임 만들기")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void newEventSubmit() throws Exception{
        Crew crew = crewService.findByTitle("test");

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/new-event")
                        .param("title", "testMeeting")
                        .param("description", "testMeeting")
                        .param("endEnrollmentDateTime", LocalDateTime.now().plusHours(12).format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("startDateTime", LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDateTime", LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ISO_DATE_TIME))
                .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

}