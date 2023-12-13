package portfolio2023.runningmate.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.dto.CrewForm;
import portfolio2023.runningmate.domain.validator.CrewFormValidator;
import portfolio2023.runningmate.security.CurrentAccount;
import portfolio2023.runningmate.service.CrewService;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/running-mate")
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;
    private final ModelMapper modelMapper;
    private final CrewFormValidator crewFormValidator;

    @InitBinder("crewForm")
    public void crewFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(crewFormValidator);
    }

    @GetMapping("/new-crew")
    public String newCrewForm(@CurrentAccount Account account, Model model){

        model.addAttribute(account);
        model.addAttribute(new CrewForm());
        return "crew/form";
    }

    @PostMapping("/new-crew")
    public String newCrewSubmit(@CurrentAccount Account account, @Valid CrewForm crewForm, Errors errors,Model model){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return "crew/form";
        }

        Crew newCrew = crewService.createNewCrew(modelMapper.map(crewForm, Crew.class), account);
        return "redirect:/running-mate/crew/" + URLEncoder.encode(newCrew.getTitle(), StandardCharsets.UTF_8);
    }

    @GetMapping("/crew/{title}")
    public String viewCrew(@CurrentAccount Account account, @PathVariable String title, Model model){
        Crew crew = crewService.getCrew(title);
        model.addAttribute(account);
        model.addAttribute(crew);
        return "crew/view";
    }

    @GetMapping("/crew/{title}/members")
    public String viewCrewMembers(@CurrentAccount Account account, @PathVariable String title, Model model){
        model.addAttribute(account);
        model.addAttribute(crewService.getCrew(title));

        return "crew/members";
    }

    @GetMapping("/crew/{title}/join")
    public String joinCrew(@CurrentAccount Account account, @PathVariable String title){
        Crew crew = crewService.findMembersByTitle(title);
        crewService.addMember(crew, account);
        return "redirect:/running-mate/crew/" + crew.getEncodedTitle() + "/members";
    }

    @GetMapping("/crew/{title}/leave")
    public String leaveCrew(@CurrentAccount Account account, @PathVariable String title){
        Crew crew = crewService.findMembersByTitle(title);
        crewService.removeAccount(crew, account);
        return "redirect:/running-mate/crew/"+ crew.getEncodedTitle() + "/members";
    }

    @GetMapping("/search/crew")
    public String searchCrew(String keyword, Model model,
            @PageableDefault(size = 9,sort = "publishedDateTime", direction = Sort.Direction.DESC) Pageable pageable){
        Page<Crew> crewPage = crewService.searchKeyword(keyword, pageable);
        model.addAttribute("crewPage",crewPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "crew/search";
    }

}
