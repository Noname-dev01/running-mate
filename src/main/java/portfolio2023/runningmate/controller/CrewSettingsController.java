package portfolio2023.runningmate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.Tag;
import portfolio2023.runningmate.domain.Zone;
import portfolio2023.runningmate.domain.dto.CrewDescriptionForm;
import portfolio2023.runningmate.domain.dto.TagForm;
import portfolio2023.runningmate.domain.dto.ZoneForm;
import portfolio2023.runningmate.security.CurrentAccount;
import portfolio2023.runningmate.service.CrewService;
import portfolio2023.runningmate.service.TagService;
import portfolio2023.runningmate.service.ZoneService;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/running-mate/crew/{title}/settings")
@RequiredArgsConstructor
public class CrewSettingsController {

    private final CrewService crewService;
    private final ModelMapper modelMapper;
    private final TagService tagService;
    private final ObjectMapper objectMapper;
    private final ZoneService zoneService;

    @GetMapping("/description")
    public String viewCrewSetting(@CurrentAccount Account account, @PathVariable String title, Model model){
        Crew crew = crewService.getCrewToUpdate(account, title);
        model.addAttribute(account);
        model.addAttribute(crew);
        model.addAttribute(modelMapper.map(crew, CrewDescriptionForm.class));
        return "crew/settings/description";
    }

    @PostMapping("/description")
    public String updateCrewInfo(@CurrentAccount Account account, @PathVariable String title,
                                 @Valid CrewDescriptionForm crewDescriptionForm, Errors errors,
                                 Model model, RedirectAttributes attributes){
        Crew crew = crewService.getCrewToUpdate(account, title);

        if (errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(crew);
            return "crew/settings/description";
        }

        crewService.updateCrewDescription(crew, crewDescriptionForm);
        attributes.addFlashAttribute("message", "크루 소개를 수정했습니다.");
        return "redirect:/running-mate/crew/" + getTitle(title) + "/settings/description";
    }

    @GetMapping("/banner")
    public String crewBannerForm(@CurrentAccount Account account, @PathVariable String title, Model model){
        Crew crew = crewService.getCrewToUpdate(account, title);
        model.addAttribute(account);
        model.addAttribute(crew);
        return "crew/settings/banner";
    }

    @PostMapping("/banner")
    public String crewBannerSubmit(@CurrentAccount Account account, @PathVariable String title,
                                   String image, RedirectAttributes attributes){
        Crew crew = crewService.getCrewToUpdate(account, title);
        crewService.updateCrewImage(crew, image);
        attributes.addFlashAttribute("message", "크루 이미지를 수정했습니다.");
        return "redirect:/running-mate/crew/"+ getTitle(title) + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableCrewBanner(@CurrentAccount Account account, @PathVariable String title){
        Crew crew = crewService.getCrewToUpdate(account, title);
        crewService.enableCrewBanner(crew);
        return "redirect:/running-mate/crew/"+ getTitle(title) + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableCrewBanner(@CurrentAccount Account account, @PathVariable String title){
        Crew crew = crewService.getCrewToUpdate(account, title);
        crewService.disableCrewBanner(crew);
        return "redirect:/running-mate/crew/" + getTitle(title) + "/settings/banner";
    }

    @GetMapping("/tags")
    public String crewTagsForm(@CurrentAccount Account account, @PathVariable String title, Model model) throws JsonProcessingException {
        Crew crew = crewService.getCrewToUpdate(account, title);
        model.addAttribute(account);
        model.addAttribute(crew);

        model.addAttribute("tags", crew.getTags().stream()
                .map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTagTitles = tagService.findAllTagTitles();
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagTitles));
        return "crew/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @PathVariable String title,
                               @RequestBody TagForm tagForm){
        Crew crew = crewService.getCrewToUpdateTag(account, title);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        crewService.addTag(crew, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    public ResponseEntity removeTag(@CurrentAccount Account account, @PathVariable String title,
                                    @RequestBody TagForm tagForm){
        Crew crew = crewService.getCrewToUpdateTag(account, title);
        Tag tag = tagService.findByTitle(tagForm);
        if (tag ==  null){
            return ResponseEntity.badRequest().build();
        }

        crewService.removeTag(crew, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String crewZonesForm(@CurrentAccount Account account, @PathVariable String title, Model model) throws JsonProcessingException {
        Crew crew = crewService.getCrewToUpdate(account, title);
        model.addAttribute(account);
        model.addAttribute(crew);
        model.addAttribute("zones", crew.getZones().stream()
                .map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneService.findAllZones();
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        return "crew/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @PathVariable String title,
                                  @RequestBody ZoneForm zoneForm){
        Crew crew = crewService.getCrewToUpdateZone(account, title);
        Zone zone = zoneService.findByCityAndProvince(zoneForm);
        if (zone == null){
            return ResponseEntity.badRequest().build();
        }

        crewService.addZone(crew, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @PathVariable String title,
                                     @RequestBody ZoneForm zoneForm){
        Crew crew = crewService.getCrewToUpdateZone(account, title);
        Zone zone = zoneService.findByCityAndProvince(zoneForm);
        if (zone == null){
            return ResponseEntity.badRequest().build();
        }

        crewService.removeZone(crew, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public String crewStatusForm(@CurrentAccount Account account, @PathVariable String title, Model model){
        Crew crew = crewService.getCrewToUpdate(account, title);
        model.addAttribute(account);
        model.addAttribute(crew);
        return "crew/settings/status";
    }

    @PostMapping("/status/publish")
    public String publishCrew(@CurrentAccount Account account, @PathVariable String title, RedirectAttributes attributes){
        Crew crew = crewService.getCrewToUpdateStatus(account, title);
        crewService.publish(crew);
        attributes.addFlashAttribute("message", "크루를 공개했습니다.");
        return "redirect:/running-mate/crew/"+getTitle(title)+"/settings/status";
    }

    @PostMapping("/status/close")
    public String closeCrew(@CurrentAccount Account account, @PathVariable String title, RedirectAttributes attributes){
        Crew crew = crewService.getCrewToUpdateStatus(account, title);
        crewService.close(crew);
        attributes.addFlashAttribute("message", "크루를 종료했습니다.");
        return "redirect:/running-mate/crew/"+getTitle(title)+"/settings/status";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentAccount Account account, @PathVariable String title, RedirectAttributes attributes){
        Crew crew = crewService.getCrewToUpdateStatus(account, title);
        if (!crew.canUpdateRecruiting()){
            attributes.addFlashAttribute("message", "5분 안에 크루원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/running-mate/crew/"+getTitle(title)+"/settings/status";
        }

        crewService.startRecruit(crew);
        attributes.addFlashAttribute("message", "크루원 모집을 시작합니다.");
        return "redirect:/running-mate/crew/"+getTitle(title)+"/settings/status";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentAccount Account account, @PathVariable String title, RedirectAttributes attributes){
        Crew crew = crewService.getCrewToUpdateStatus(account, title);
        if (!crew.canUpdateRecruiting()){
            attributes.addFlashAttribute("message", "5분 안에 크루원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/running-mate/crew/"+getTitle(title)+"/settings/status";
        }

        crewService.stopRecruit(crew);
        attributes.addFlashAttribute("message", "크루원 모집을 종료합니다.");
        return "redirect:/running-mate/crew/"+getTitle(title)+"/settings/status";
    }

    @PostMapping("/status/title")
    public String updateCrewTitle(@CurrentAccount Account account, @PathVariable String title, @RequestParam String newTitle,
                                  Model model, RedirectAttributes attributes){
        Crew crew = crewService.getCrewToUpdateStatus(account, title);
        if (!crewService.isValidTitle(newTitle)){
            model.addAttribute(account);
            model.addAttribute(crew);
            model.addAttribute("crewTitleError", "이미 사용중인 크루명이거나 글자수가 50자 초과했습니다. 다시 입력해주세요.");
            return "crew/settings/status";
        }

        crewService.updateCrewTitle(crew, newTitle);
        attributes.addFlashAttribute("message", "크루 이름을 수정했습니다.");
        return "redirect:/running-mate/crew/"+crew.getEncodedTitle()+"/settings/status";
    }

    @PostMapping("/status/remove")
    public String removeStatus(@CurrentAccount Account account, @PathVariable String title){
        Crew crew = crewService.getCrewToUpdateStatus(account, title);
        crewService.remove(crew);
        return "redirect:/running-mate";
    }

    private String getTitle(String title) {
        return URLEncoder.encode(title, StandardCharsets.UTF_8);
    }

}
