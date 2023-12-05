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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Tag;
import portfolio2023.runningmate.domain.Zone;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.domain.dto.TagForm;
import portfolio2023.runningmate.domain.dto.ZoneForm;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.repository.TagRepository;
import portfolio2023.runningmate.repository.ZoneRepository;
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

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountService accountService;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @BeforeEach
    void beforeEach(){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("admin");
        signUpForm.setEmail("admin@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
        zoneRepository.save(testZone);
    }

    @AfterEach()
    void afterEach(){
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
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

    @Test
    @DisplayName("알림 설정 폼")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateNotifications_form() throws Exception {
        mockMvc.perform(get("/running-mate/settings/notifications"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notifications"));
    }

    @Test
    @DisplayName("알림 설정 - 정상")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateNotifications_success() throws Exception {
        mockMvc.perform(post("/running-mate/settings/notifications")
                        .param("crewUpdatedByEmail", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/running-mate/settings/notifications"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountService.findByNickname("admin");
        assertFalse(account.isCrewCreatedByEmail());
        assertFalse(account.isCrewRecruitByEmail());
        assertTrue(account.isCrewUpdatedByEmail());
    }

    @Test
    @DisplayName("닉네임 수정 폼")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateAccount_form() throws Exception {
        mockMvc.perform(get("/running-mate/settings/account"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(view().name("settings/account"));
    }

    @Test
    @DisplayName("닉네임 수정 - 정상")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateAccount_success() throws Exception{
        mockMvc.perform(post("/running-mate/settings/account")
                        .param("nickname", "admin1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().hasNoErrors())
                .andExpect(flash().attributeExists("message"));

        Account account = accountService.findByEmail("admin@email.com");
        assertEquals("admin1", account.getNickname());
    }

    @Test
    @DisplayName("닉네임 수정 - 입력값 에러")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateAccount_fail() throws Exception{
        mockMvc.perform(post("/running-mate/settings/account")
                        .param("nickname", "admin")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @DisplayName("러닝의 목적 수정 폼")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateTag_form() throws Exception{
        mockMvc.perform(get("/running-mate/settings/tags"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whiteList"))
                .andExpect(model().attributeExists("tags"));
    }

    @Test
    @DisplayName("계정에 러닝의 목적 추가")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void addTag() throws Exception{
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/running-mate/settings/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("admin").getTags().contains(newTag));
    }

    @Test
    @DisplayName("계정에 러닝의 목적 삭제")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void removeTag() throws Exception{
        Account account = accountRepository.findByNickname("admin");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(account, newTag);

        assertTrue(account.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");
        mockMvc.perform(post("/running-mate/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(account.getTags().contains(newTag));
    }

    @Test
    @DisplayName("계정에 러닝 희망 지역 폼")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void updateZoneForm() throws Exception{
        mockMvc.perform(get("/running-mate/settings/zones"))
                .andExpect(view().name("settings/zones"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whiteList"))
                .andExpect(model().attributeExists("zones"));
    }

    @Test
    @DisplayName("계정에 러닝 희망 지역 추가")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post("/running-mate/settings/zones/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account account = accountRepository.findByNickname("admin");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(account.getZones().contains(zone));
    }

    @Test
    @DisplayName("계정에 러닝 희망 지역 삭제")
    @WithUserDetails(value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void removeZone() throws Exception {
        Account account = accountRepository.findByNickname("admin");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(account, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post("/running-mate/settings/zones/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(account.getZones().contains(zone));
    }
}