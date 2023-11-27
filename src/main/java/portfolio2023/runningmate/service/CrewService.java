package portfolio2023.runningmate.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.Tag;
import portfolio2023.runningmate.domain.Zone;
import portfolio2023.runningmate.domain.dto.CrewDescriptionForm;
import portfolio2023.runningmate.repository.CrewRepository;
import portfolio2023.runningmate.repository.TagRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CrewService {

    private final CrewRepository crewRepository;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;

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

    public void addTag(Crew crew, Tag tag) {
        crew.getTags().add(tag);
    }

    public void removeTag(Crew crew, Tag tag){
        crew.getTags().remove(tag);
    }

    public void addZone(Crew crew, Zone zone){
        crew.getZones().add(zone);
    }

    public void removeZone(Crew crew, Zone zone){
        crew.getZones().remove(zone);
    }

    public Crew getCrewToUpdateTag(Account account, String title){
        Crew crew = crewRepository.findCrewWithTagsByTitle(title);
        checkIfExistingCrew(title, crew);
        checkIfManager(account, crew);
        return crew;
    }

    public Crew getCrewToUpdateZone(Account account, String title){
        Crew crew = crewRepository.findCrewWithZonesByTitle(title);
        checkIfExistingCrew(title, crew);
        checkIfManager(account, crew);
        return crew;
    }

    public Crew getCrewToUpdateStatus(Account account, String title) {
        Crew crew = crewRepository.findCrewWithManagerByTitle(title);
        checkIfExistingCrew(title, crew);
        checkIfManager(account, crew);
        return crew;
    }

    private void checkIfManager(Account account, Crew crew) {
        if (!account.isManagerOf(crew)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingCrew(String title, Crew crew) {
        if (crew == null){
            throw new IllegalArgumentException(title + "에 해당하는 크루가 없습니다.");
        }
    }

    public void publish(Crew crew) {
        crew.publish();
    }

    public void close(Crew crew){
        crew.close();
    }

    public void startRecruit(Crew crew){
        crew.startRecruit();
    }

    public void stopRecruit(Crew crew){
        crew.stopRecruit();
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50 && !crewRepository.existsByTitle(newTitle);
    }

    public void updateCrewTitle(Crew crew, String newTitle) {
        crew.setTitle(newTitle);
    }

    public void remove(Crew crew) {
        if (crew.isRemovable()){
            crewRepository.delete(crew);
        }else {
            throw new IllegalArgumentException("크루를 삭제할 수 없습니다.");
        }
    }
}
