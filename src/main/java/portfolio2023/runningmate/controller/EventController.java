package portfolio2023.runningmate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.Enrollment;
import portfolio2023.runningmate.domain.Event;
import portfolio2023.runningmate.domain.dto.EventForm;
import portfolio2023.runningmate.domain.validator.EventValidator;
import portfolio2023.runningmate.security.CurrentAccount;
import portfolio2023.runningmate.service.CrewService;
import portfolio2023.runningmate.service.EnrollmentService;
import portfolio2023.runningmate.service.EventService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/running-mate/crew/{title}")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final CrewService crewService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final EnrollmentService enrollmentService;

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
    public String getEvent(@CurrentAccount Account account, @PathVariable String title, @PathVariable("id") Event event, Model model){
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(crewService.getCrew(title));
        return "event/view";
    }

    @GetMapping("/events")
    public String viewCrewEvents(@CurrentAccount Account account, @PathVariable String title, Model model){
        Crew crew = crewService.getCrew(title);
        model.addAttribute(account);
        model.addAttribute(crew);

        List<Event> events = eventService.findCrewEvents(crew);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        events.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now())){
                oldEvents.add(e);
            }else {
                newEvents.add(e);
            }
        });

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);

        return "crew/events";
    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentAccount Account account,
                                  @PathVariable String title, @PathVariable("id") Event event, Model model){
        Crew crew = crewService.getCrewToUpdate(account, title);
        model.addAttribute(crew);
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentAccount Account account, @PathVariable String title,
                                    @PathVariable("id") Event event, @Valid EventForm eventForm, Errors errors, Model model){
        Crew crew = crewService.getCrewToUpdate(account, title);
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, event, errors);

        if (errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(crew);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event, eventForm);
        return "redirect:/running-mate/crew/"+crew.getEncodedTitle() + "/events/"+ event.getId();
    }

    @PostMapping("/events/{id}/delete")
    public String cancelEvent(@CurrentAccount Account account, @PathVariable String title, @PathVariable("id") Event event){
        Crew crew = crewService.getCrewToUpdateStatus(account, title);
        eventService.deleteEvent(event);
        return "redirect:/running-mate/crew/"+ crew.getEncodedTitle() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentAccount Account account, @PathVariable String title, @PathVariable("id") Event event){
        Crew crew = crewService.getCrewToEnroll(title);
        eventService.newEnrollment(event, account);
        return "redirect:/running-mate/crew/"+ crew.getEncodedTitle() + "/events/"+event.getId();
    }

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentAccount Account account, @PathVariable String title, @PathVariable("id") Event event){
        Crew crew = crewService.getCrewToEnroll(title);
        eventService.cancelEnrollment(event, account);
        return "redirect:/running-mate/crew/"+crew.getEncodedTitle()+"/events/"+event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentAccount Account account, @PathVariable String title,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment){
        Crew crew = crewService.getCrewToUpdate(account, title);
        eventService.acceptEnrollment(event, enrollment);
        return "redirect:/running-mate/crew/"+crew.getEncodedTitle()+"/events/"+ event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentAccount Account account, @PathVariable String title,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment){
        Crew crew = crewService.getCrewToUpdate(account, title);
        eventService.rejectEnrollment(event, enrollment);
        return "redirect:/running-mate/crew/"+ crew.getEncodedTitle() + "/events/"+ event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentAccount Account account, @PathVariable String title,
                                    @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment){
        Crew crew = crewService.getCrewToUpdate(account, title);
        eventService.checkInEnrollment(enrollment);
        return "redirect:/running-mate/crew/"+crew.getEncodedTitle()+"/events/"+event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentAccount Account account, @PathVariable String title,
                                          @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment){
        Crew crew = crewService.getCrewToUpdate(account, title);
        eventService.cancelCheckInEnrollment(enrollment);
        return "redirect:/running-mate/crew/"+crew.getEncodedTitle()+"/events/"+event.getId();
    }
}
