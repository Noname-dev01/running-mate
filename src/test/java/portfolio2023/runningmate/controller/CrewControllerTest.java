package portfolio2023.runningmate.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.service.AccountService;
import portfolio2023.runningmate.service.CrewService;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CrewControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired CrewService crewService;


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
    @DisplayName("크루 만들기 폼")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void newCrewForm() throws Exception {
        mockMvc.perform(get("/running-mate/new-crew")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("crew/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crewForm"));
    }

    @Test
    @DisplayName("크루 만들기 - 정상")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void newCrewSubmit_success() throws Exception {
        mockMvc.perform(post("/running-mate/new-crew")
                        .param("title", "test-Crew")
                        .param("shortDescription", "short description")
                        .param("fullDescription", "fullDescriptionfullDescriptionfullDescriptionfullDescriptionfullDescriptionfullDescriptionfullDescription")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/test-Crew"));

        Crew crew = crewService.findByTitle("test-Crew");
        assertNotNull(crew);
        Account account = accountService.findByNickname("admin");
        assertTrue(crew.getManager().contains(account));
    }

    @Test
    @DisplayName("크루 만들기 - 실패")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void newCrewSubmit_fail() throws Exception {
        mockMvc.perform(post("/running-mate/new-crew")
                        .param("title", "test Crew")
                        .param("shortDescription", "short description")
                        .param("fullDescription", "fullDescriptionfullDescriptionfullDescriptionfullDescriptionfullDescriptionfullDescriptionfullDescription")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("crew/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("crewForm"))
                .andExpect(model().attributeExists("account"));

        Crew crew = crewService.findByTitle("test Crew");
        assertNull(crew);
    }



    @Test
    @DisplayName("크루 조회")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void viewCrew() throws Exception {
        Crew crew = new Crew();
        crew.setTitle("test-Crew");
        crew.setShortDescription("short description");
        crew.setFullDescription("full description");

        Account account = accountService.findByNickname("admin");
        crewService.createNewCrew(crew,account);

        mockMvc.perform(get("/running-mate/crew/test-Crew"))
                .andExpect(view().name("crew/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"));
    }

    @Test
    @DisplayName("크루원 조회")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void viewCrewMembers() throws Exception {
        Crew crew = new Crew();
        crew.setTitle("testCrew");
        crew.setShortDescription("shrot description");
        crew.setFullDescription("full description");

        Account account = accountService.findByNickname("admin");
        crewService.createNewCrew(crew, account);

        mockMvc.perform(get("/running-mate/crew/testCrew/members"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"));

    }

    @Test
    @DisplayName("크루 가입")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void joinCrew() throws Exception{
        Crew crew = new Crew();
        crew.setTitle("testCrew");
        crew.setShortDescription("shrot description");
        crew.setFullDescription("full description");

        Account account = accountService.findByNickname("admin");
        crewService.createNewCrew(crew, account);

        mockMvc.perform(get("/running-mate/crew/testCrew/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/members"));

        assertTrue(crew.getMembers().contains(account));
    }

}