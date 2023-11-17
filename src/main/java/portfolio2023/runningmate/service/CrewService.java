package portfolio2023.runningmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.repository.CrewRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CrewService {

    private final CrewRepository crewRepository;

    public Crew createNewCrew(Crew crew, Account account) {
        Crew newCrew = crewRepository.save(crew);
        newCrew.setManager(account);
        return newCrew;
    }
}
