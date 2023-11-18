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

    public Crew findByTitle(String title){
        return crewRepository.findByTitle(title);
    }

    public Crew getCrew(String title) {
        Crew crew = crewRepository.findByTitle(title);
        checkIfExistingStudy(title, crew);
        return crew;
    }

    public Crew findMembersByTitle(String title){
        return crewRepository.findCrewWithMembersByTitle(title);
    }

    public void addMember(Crew crew, Account account){
        crew.addMemberCount(account);
    }

    private void checkIfExistingStudy(String title, Crew crew) {
        if (crew == null){
            throw new IllegalArgumentException(title + "에 해당하는 크루가 없습니다.");
        }
    }
}
