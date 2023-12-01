package portfolio2023.runningmate.controller;

import org.assertj.core.api.Assertions;
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
import portfolio2023.runningmate.domain.Event;
import portfolio2023.runningmate.domain.EventType;
import portfolio2023.runningmate.domain.dto.EventForm;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.factory.AccountFactory;
import portfolio2023.runningmate.factory.CrewFactory;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.repository.CrewRepository;
import portfolio2023.runningmate.service.AccountService;
import portfolio2023.runningmate.service.CrewService;
import portfolio2023.runningmate.service.EventService;

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
    @Autowired EventService eventService;
    @Autowired AccountFactory accountFactory;
    @Autowired CrewFactory crewFactory;

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

    @Test
    @DisplayName("모임 만들기 - 실패")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void newEventSubmit_fail() throws Exception {
        Crew crew = crewService.findByTitle("test");

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/new-event")
                        .param("title", "testMeeting")
                        .param("description", "testMeeting")
                        .param("endEnrollmentDateTime", LocalDateTime.now().plusHours(12).format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("startDateTime", LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDateTime", LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ISO_DATE_TIME))
                        .with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("모임 취소")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void cancelEvent() throws Exception {
        Account account = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, crew, account);

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()+"/delete")
                .with(csrf()))
                .andExpect(status().is3xxRedirection());

    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Crew crew, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, crew, account);
    }

}