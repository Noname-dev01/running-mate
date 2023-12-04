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
import portfolio2023.runningmate.domain.*;
import portfolio2023.runningmate.domain.dto.EventForm;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.factory.AccountFactory;
import portfolio2023.runningmate.factory.CrewFactory;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.repository.CrewRepository;
import portfolio2023.runningmate.repository.EnrollmentRepository;
import portfolio2023.runningmate.service.AccountService;
import portfolio2023.runningmate.service.CrewService;
import portfolio2023.runningmate.service.EnrollmentService;
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
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired
    EnrollmentService enrollmentService;

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
        Account account = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", account);

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
        Account account = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", account);

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
        Account account = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", account);

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
    @DisplayName("모임 조회 뷰")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void getEvent() throws Exception{
        Account account = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, crew, account);

        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("crew"))
                .andExpect(view().name("event/view"));
    }

    @Test
    @DisplayName("크루 모임 목록 조회")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void viewCrewEvents() throws Exception{
        Account account = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, crew, account);

        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/events")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"))
                .andExpect(model().attributeExists("newEvents"))
                .andExpect(model().attributeExists("oldEvents"))
                .andExpect(view().name("crew/events"));

    }

    @Test
    @DisplayName("모임 수정 뷰")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateEventForm() throws Exception{
        Account account = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, crew, account);

        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()+"/edit")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("crew"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("eventForm"))
                .andExpect(view().name("event/update-form"));
    }

    @Test
    @DisplayName("모임 수정")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateEventSubmit() throws Exception{
        Account account = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, crew, account);

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()+"/edit")
                        .param("title", "update-test-event")
                        .param("endEnrollmentDateTime", event.getEndEnrollmentDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("startDateTime", event.getStartDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDateTime", event.getEndDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertEquals(event.getTitle(), "update-test-event");
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

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void newEnrollment_FCFS_event_accpted() throws Exception{
        Account account = accountFactory.createAccount("test");
        Crew crew = crewFactory.createCrew("test-crew", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, crew, account);

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()+"/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()));

        Account admin = accountService.findByNickname("admin");
        assertTrue(enrollmentRepository.findByEventAndAccount(event,admin).isAccepted());
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 (모집 인원이 초과한 경우)")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void newEnrollment_FCFS_event_not_accepted() throws Exception{
        Account test = accountFactory.createAccount("test");
        Crew crew = crewFactory.createCrew("test-crew", test);
        Event event = createEvent("test-event", EventType.FCFS, 2, crew, test);

        Account account1 = accountFactory.createAccount("account1");
        Account account2 = accountFactory.createAccount("account2");
        eventService.newEnrollment(event, account1);
        eventService.newEnrollment(event, account2);

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()+"/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()));

        Account account = accountService.findByNickname("admin");
        isNotAccepted(account, event);
    }

    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account account = accountRepository.findByNickname("admin");
        Account test = accountFactory.createAccount("test");
        Account may = accountFactory.createAccount("may");
        Crew crew = crewFactory.createCrew("test-crew", test);
        Event event = createEvent("test-event", EventType.FCFS, 2, crew, test);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, account);
        eventService.newEnrollment(event, test);

        isAccepted(may, event);
        isAccepted(account, event);
        isNotAccepted(test, event);

        mockMvc.perform(post("/running-mate/crew/" + crew.getTitle() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/" + crew.getTitle() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(test, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, account));
    }

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void not_accepterd_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account admin = accountRepository.findByNickname("admin");
        Account test = accountFactory.createAccount("test");
        Account may = accountFactory.createAccount("may");
        Crew crew = crewFactory.createCrew("test-crew", test);
        Event event = createEvent("test-event", EventType.FCFS, 2, crew, test);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, test);
        eventService.newEnrollment(event, admin);

        isAccepted(may, event);
        isAccepted(test, event);
        isNotAccepted(admin, event);

        mockMvc.perform(post("/running-mate/crew/" + crew.getTitle() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/" + crew.getTitle() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(test, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, admin));
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void newEnrollment_to_CONFIMATIVE_event_not_accepted() throws Exception {
        Account test = accountFactory.createAccount("test");
        Crew crew = crewFactory.createCrew("test-crew", test);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, crew, test);

        mockMvc.perform(post("/running-mate/crew/" + crew.getTitle() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/" + crew.getTitle() + "/events/" + event.getId()));

        Account admin = accountRepository.findByNickname("admin");
        isNotAccepted(admin, event);
    }

    @Test
    @DisplayName("관리자 승인 모임에 참가 신청 - 승인 받은후 참가 상태 확정(참가신청 수락)")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void CONFIRMATIVE_event_accepted() throws Exception{
        Account admin = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", admin);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, crew, admin);
        eventService.newEnrollment(event, admin);
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, admin);

        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()+"/enrollments/"+enrollment.getId()+"/accept")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()));

        assertTrue(enrollment.isAccepted());
    }

    @Test
    @DisplayName("관리자 승인 모임에 참가 신청 - 참가신청 취소")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void rejectEnrollment_notAccepted() throws Exception{
        Account admin = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", admin);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, crew, admin);
        eventService.newEnrollment(event, admin);
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, admin);

        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()+"/enrollments/"+enrollment.getId()+"/reject")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()));

        assertFalse(enrollment.isAccepted());
    }

    @Test
    @DisplayName("모임 출석체크 (모임 관리자만 가능)")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void checkInEnrollment() throws Exception{
        Account admin = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", admin);
        Event event = createEvent("test-event", EventType.FCFS, 2, crew, admin);
        eventService.newEnrollment(event, admin);
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, admin);

        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()+"/enrollments/"+enrollment.getId()+"/checkin")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()));

        assertTrue(enrollment.isAttended());
    }

    @Test
    @DisplayName("모임 출석체크 취소 (모임 관리자만 가능)")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void cancelCheckInEnrollment() throws Exception {
        Account admin = accountService.findByNickname("admin");
        Crew crew = crewFactory.createCrew("test-crew", admin);
        Event event = createEvent("test-event", EventType.FCFS, 2, crew, admin);
        eventService.newEnrollment(event, admin);
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, admin);

        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()+"/enrollments/"+enrollment.getId()+"/cancel-checkin")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/events/"+event.getId()));

        assertFalse(enrollment.isAttended());
    }

    private void isNotAccepted(Account account, Event event) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    private void isAccepted(Account account, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
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