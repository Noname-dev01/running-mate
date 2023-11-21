package portfolio2023.runningmate.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.dto.CrewDescriptionForm;
import portfolio2023.runningmate.security.CurrentAccount;
import portfolio2023.runningmate.service.CrewService;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/running-mate/crew/{title}/settings")
@RequiredArgsConstructor
public class CrewSettingsController {

    private final CrewService crewService;
    private final ModelMapper modelMapper;

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

    private String getTitle(String title) {
        return URLEncoder.encode(title, StandardCharsets.UTF_8);
    }

}
