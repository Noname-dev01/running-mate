package portfolio2023.runningmate.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String newCrewSubmit(@CurrentAccount Account account, @Valid CrewForm crewForm, Errors errors){
        if (errors.hasErrors()){
            return "crew/form";
        }

        Crew newCrew = crewService.createNewCrew(modelMapper.map(crewForm, Crew.class), account);
        return "redirect:/running-mate/crew/" + URLEncoder.encode(newCrew.getPath(), StandardCharsets.UTF_8);
    }

}
