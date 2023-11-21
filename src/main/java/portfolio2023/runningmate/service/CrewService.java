package portfolio2023.runningmate.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.dto.CrewDescriptionForm;
import portfolio2023.runningmate.repository.CrewRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CrewService {

    private final CrewRepository crewRepository;
    private final ModelMapper modelMapper;

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
        checkIfExistingCrew(title, crew);
        return crew;
    }

    public Crew findMembersByTitle(String title){
        return crewRepository.findCrewWithMembersByTitle(title);
    }

    public void addMember(Crew crew, Account account){
        crew.addMemberCount(account);
    }

    private void checkIfExistingCrew(String title, Crew crew) {
        if (crew == null){
            throw new IllegalArgumentException(title + "에 해당하는 크루가 없습니다.");
        }
    }

    public Crew getCrewToUpdate(Account account, String title) {
        Crew crew = this.getCrew(title);
        if (!account.isManagerOf(crew)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return crew;
    }

    public void updateCrewDescription(Crew crew, CrewDescriptionForm crewDescriptionForm){
        modelMapper.map(crewDescriptionForm, crew);
    }

    public void updateCrewImage(Crew crew, String image) {
        crew.setImage(image);
    }

    public void enableCrewBanner(Crew crew){
        crew.setUseBanner(true);
    }

    public void disableCrewBanner(Crew crew){
        crew.setUseBanner(false);
    }
}
