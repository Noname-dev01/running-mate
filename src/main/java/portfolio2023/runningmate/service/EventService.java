package portfolio2023.runningmate.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.Event;
import portfolio2023.runningmate.domain.dto.EventForm;
import portfolio2023.runningmate.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;

    public Event createEvent(Event event, Crew crew, Account account) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setCrew(crew);
        return eventRepository.save(event);
    }

    public Event findById(Long id) {
        return eventRepository.findById(id).orElseThrow();
    }

    public List<Event> findCrewEvents(Crew crew) {
        return eventRepository.findByCrewOrderByStartDateTime(crew);
    }


    public Event findEventById(Long id) {
        return eventRepository.findById(id).orElseThrow();
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }
}
