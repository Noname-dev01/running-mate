package portfolio2023.runningmate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.Tag;
import portfolio2023.runningmate.domain.Zone;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.domain.dto.TagForm;
import portfolio2023.runningmate.domain.dto.ZoneForm;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.repository.CrewRepository;
import portfolio2023.runningmate.repository.TagRepository;
import portfolio2023.runningmate.repository.ZoneRepository;
import portfolio2023.runningmate.service.AccountService;
import portfolio2023.runningmate.service.CrewService;
import portfolio2023.runningmate.service.TagService;
import portfolio2023.runningmate.service.ZoneService;

import javax.transaction.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CrewSettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired CrewService crewService;
    @Autowired CrewRepository crewRepository;
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired ZoneRepository zoneRepository;
    @Autowired
    ZoneService zoneService;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

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
    public void viewCrewSetting() throws Exception {
        Crew crew = crewService.findByTitle("test");
        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("crew/settings/description"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"))
                .andExpect(model().attributeExists("crewDescriptionForm"));
    }

    @Test
    @DisplayName("크루 설정 - 소개 수정 성공")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateCrewInfo_success() throws Exception {
        String shortDescription = "짧은 소개 수정하기";
        String fullDescription = "상세 소개 수정하기";
        Crew crew = crewService.findByTitle("test");

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/description")
                        .param("shortDescription", shortDescription)
                        .param("fullDescription", fullDescription)
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/test/settings/description"))
                .andExpect(flash().attributeExists("message"));

        assertEquals(shortDescription, crew.getShortDescription());
        assertEquals(fullDescription, crew.getFullDescription());
    }

    @Test
    @DisplayName("크루 설정 - 소개 수정 실패")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateCrewInfo_fail() throws Exception {
        Crew crew = crewService.findByTitle("test");
        String shortDescription = "짧은 소개 수정하기";

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/description")
                .param("shortDescription", shortDescription)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"));

        assertNotEquals(shortDescription, crew.getShortDescription());
        assertEquals("full description", crew.getFullDescription());
    }

    @Test
    @DisplayName("크루 설정 - 배너 뷰")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void crewBannerForm() throws Exception {
        Crew crew = crewService.findByTitle("test");

        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/settings/banner"))
                .andExpect(status().isOk())
                .andExpect(view().name("crew/settings/banner"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"));
    }

    @Test
    @DisplayName("크루 설정 - 크루의 목적 뷰")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void crewTagsForm() throws Exception{
        Crew crew = crewService.findByTitle("test");

        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/settings/tags"))
                .andExpect(status().isOk())
                .andExpect(view().name("crew/settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @Test
    @DisplayName("크루 설정 - 크루의 목적 추가")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void addTag() throws Exception{
        Crew crew = crewService.findByTitle("test");
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("친목");

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("친목");
        assertNotNull(newTag);
        assertTrue(crewRepository.findByTitle(crew.getTitle()).getTags().contains(newTag));
    }

    @Test
    @DisplayName("크루 설정 - 크루의 목적 삭제")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void removeTag() throws Exception{
        Crew crew = crewService.findByTitle("test");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        crewService.addTag(crew, newTag);

        assertTrue(crew.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(crew.getTags().contains(newTag));
    }

    @Test
    @DisplayName("크루 설정 - 크루 러닝 희망 지역 뷰")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateZoneForm() throws Exception{
        Crew crew = crewService.findByTitle("test");

        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/settings/zones"))
                .andExpect(view().name("crew/settings/zones"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @Test
    @DisplayName("크루 설정 - 크루 러닝 희망 지역 추가")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void addZone() throws Exception {
        Crew crew = crewService.findByTitle("test");
        zoneRepository.save(testZone);
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());


        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/zones/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Zone zone1 = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(crew.getZones().contains(zone1));
    }

    @Test
    @DisplayName("크루 설정 - 크루 러닝 희망 지역 삭제")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void removeZone() throws Exception {
        Crew crew = crewService.findByTitle("test");
        Zone zone = zoneRepository.save(testZone);
        crewService.addZone(crew, zone);

        assertTrue(crew.getZones().contains(zone));

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/zones/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(crew.getZones().contains(zone));
    }

    @Test
    @DisplayName("크루 설정 - 크루 상태 관리")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void crewStatusForm() throws Exception {
        Crew crew = crewService.findByTitle("test");
        mockMvc.perform(get("/running-mate/crew/"+crew.getTitle()+"/settings/status"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("crew"))
                .andExpect(view().name("crew/settings/status"));
    }

    @Test
    @DisplayName("크루 설정 - 크루 상태 관리(크루 공개 상태로 변경)")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void publishCrew() throws Exception {
        Crew crew = crewService.findByTitle("test");
        assertFalse(crew.isPublished());

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/status/publish")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/settings/status"))
                .andExpect(flash().attributeExists("message"));

        assertTrue(crew.isPublished());
        assertFalse(crew.isClosed());
    }

    @Test
    @DisplayName("크루 설정 - 크루 상태 관리(크루 종료 상태로 변경)")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void closeCrew() throws Exception {
        Crew crew = crewService.findByTitle("test");
        crew.publish();
        assertFalse(crew.isClosed());

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/status/close")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/settings/status"))
                .andExpect(flash().attributeExists("message"));

        assertTrue(crew.isClosed());
    }

    @Test
    @DisplayName("크루 설정 - 크루 상태 관리(크루원 모집 상태로 변경)")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void startRecruit() throws Exception{
        Crew crew = crewService.findByTitle("test");
        crewService.publish(crew);

        assertTrue(crew.isPublished());
        assertFalse(crew.isRecruiting());

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/recruit/start")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/settings/status"))
                .andExpect(flash().attributeExists("message"));

        assertTrue(crew.isRecruiting());
        assertTrue(crew.isPublished());
    }

    @Test
    @DisplayName("크루 설정 - 크루 상태 관리(크루원 모집 중단 상태로 변경)")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void stopRecruit() throws Exception{
        Crew crew = crewService.findByTitle("test");
        crewService.publish(crew);
        crewService.startRecruit(crew);
        crew.setRecruitingUpdatedDateTime(LocalDateTime.now().minusMinutes(10));

        assertTrue(crew.isPublished());
        assertTrue(crew.isRecruiting());

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/recruit/stop")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/settings/status"))
                .andExpect(flash().attributeExists("message"));

        assertTrue(crew.isPublished());
        assertFalse(crew.isRecruiting());
    }

    @Test
    @DisplayName("크루 설정 - 크루 이름 변경")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateCrewTitle() throws Exception{
        String oldTitle = "test";
        String newTitle = "test123";

        Crew crew = crewService.findByTitle(oldTitle);
        assertEquals(crew.getTitle(), oldTitle);

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/status/title")
                        .param("newTitle", newTitle)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/crew/"+crew.getTitle()+"/settings/status"))
                .andExpect(flash().attributeExists("message"));

        assertEquals(crew.getTitle(),newTitle);
    }

    @Test
    @DisplayName("크루 설정 - 크루 삭제")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void removeStatus() throws Exception{
        Crew crew = crewService.findByTitle("test");

        assertFalse(crew.isPublished());

        mockMvc.perform(post("/running-mate/crew/"+crew.getTitle()+"/settings/status/remove")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate"));

        assertFalse(crewRepository.existsByTitle("test"));
    }
}