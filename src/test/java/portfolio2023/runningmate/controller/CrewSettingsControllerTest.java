package portfolio2023.runningmate.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import portfolio2023.runningmate.repository.CrewRepository;
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
class CrewSettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired CrewService crewService;
    @Autowired CrewRepository crewRepository;

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
    @DisplayName("크루 설정 - 소개 뷰")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void viewCrewSetting() throws Exception {
        mockMvc.perform(get("/running-mate/crew/test/settings/description"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"))
                .andExpect(model().attributeExists("crewDescriptionForm"));
    }

    @Test
    @DisplayName("크루 설정 - 소개 수정 성공")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateCrewInfo_success() throws Exception {
        String shortDescription = "짧은 소개 수정하기";
        String fullDescription = "상세 소개 수정하기";

        mockMvc.perform(post("/running-mate/crew/test/settings/description")
                        .param("shortDescription", shortDescription)
                        .param("fullDescription", fullDescription)
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/test/settings/description"))
                .andExpect(flash().attributeExists("message"));

        Crew crew = crewService.findByTitle("test");
        assertEquals(shortDescription, crew.getShortDescription());
        assertEquals(fullDescription, crew.getFullDescription());
    }

    @Test
    @DisplayName("크루 설정 - 소개 수정 실패")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateCrewInfo_fail() throws Exception {
        String shortDescription = "짧은 소개 수정하기";

        mockMvc.perform(post("/running-mate/crew/test/settings/description")
                .param("shortDescription", shortDescription)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"));

        Crew crew = crewService.findByTitle("test");
        assertNotEquals(shortDescription, crew.getShortDescription());
        assertEquals("full description", crew.getFullDescription());
    }

}