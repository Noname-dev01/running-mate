package portfolio2023.runningmate.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.Event;
import portfolio2023.runningmate.domain.dto.EventForm;
import portfolio2023.runningmate.domain.validator.EventValidator;
import portfolio2023.runningmate.security.CurrentAccount;
import portfolio2023.runningmate.service.CrewService;
import portfolio2023.runningmate.service.EventService;

import javax.validation.Valid;

@Controller
@RequestMapping("/running-mate/crew/{title}")
@RequiredArgsConstructor
public class EventController {

    private final CrewService crewService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String title, Model model){
        Crew crew = crewService.getCrewToUpdateStatus(account, title);
        model.addAttribute(crew);
        model.addAttribute(account);
        model.addAttribute(new EventForm());

        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentAccount Account account, @PathVariable String title,
                                 @Valid EventForm eventForm, Errors errors, Model model){
        Crew crew = crewService.getCrewToUpdateStatus(account, title);
        if (errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(crew);
            return "event/form";
        }

        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), crew, account);

        return "redirect:/running-mate/crew/"+ crew.getEncodedTitle() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentAccount Account account, @PathVariable String title, @PathVariable Long id, Model model){
        model.addAttribute(account);
        model.addAttribute(eventService.findById(id));
        model.addAttribute(crewService.getCrew(title));
        return "event/view";
    }
}
