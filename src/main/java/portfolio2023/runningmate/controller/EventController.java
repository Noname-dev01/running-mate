package portfolio2023.runningmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.dto.EventForm;
import portfolio2023.runningmate.security.CurrentAccount;
import portfolio2023.runningmate.service.CrewService;

@Controller
@RequestMapping("/running-mate/crew/{title}")
@RequiredArgsConstructor
public class EventController {

    private final CrewService crewService;

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String title, Model model){
        Crew crew = crewService.getCrewToUpdateStatus(account, title);
        model.addAttribute(crew);
        model.addAttribute(account);
        model.addAttribute(new EventForm());

        return "event/form";
    }
}
