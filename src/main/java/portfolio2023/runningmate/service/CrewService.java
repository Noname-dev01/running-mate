package portfolio2023.runningmate.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.Tag;
import portfolio2023.runningmate.domain.Zone;
import portfolio2023.runningmate.domain.dto.CrewDescriptionForm;
import portfolio2023.runningmate.domain.event.CrewCreatedEvent;
import portfolio2023.runningmate.domain.event.CrewUpdateEvent;
import portfolio2023.runningmate.repository.CrewRepository;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class CrewService {

    private final CrewRepository crewRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public Crew createNewCrew(Crew crew, Account account) {
        Crew newCrew = crewRepository.save(crew);
        newCrew.addManager(account);
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

    public Crew getCrewToUpdate(Account account, String title) {
        Crew crew = this.getCrew(title);
        checkIfManager(account, crew);
        return crew;
    }

    public void updateCrewDescription(Crew crew, CrewDescriptionForm crewDescriptionForm){
        modelMapper.map(crewDescriptionForm, crew);
        eventPublisher.publishEvent(new CrewUpdateEvent(crew, "크루 소개를 수정했습니다."));
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
        if (!crew.isManagerBy(account)){
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
        eventPublisher.publishEvent(new CrewCreatedEvent(crew));
    }

    public void close(Crew crew){
        crew.close();
        eventPublisher.publishEvent(new CrewUpdateEvent(crew, "크루를 종료했습니다."));
    }

    public void startRecruit(Crew crew){
        crew.startRecruit();
        eventPublisher.publishEvent(new CrewUpdateEvent(crew, "크루원 모집을 시작합니다."));
    }

    public void stopRecruit(Crew crew){
        crew.stopRecruit();
        eventPublisher.publishEvent(new CrewUpdateEvent(crew, "크루원 모집을 중단했습니다."));
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

    public void addMember(Crew crew, Account account){
        crew.addMemberCount(account);
    }

    public void removeAccount(Crew crew, Account account) {
        crew.removeAccount(account);
    }

    public Crew getCrewToEnroll(String title) {
        Crew crew = crewRepository.findCrewOnlyByTitle(title);
        checkIfExistingCrew(title, crew);
        return crew;
    }

    public Page<Crew> searchKeyword(String keyword, Pageable pageable) {
        return crewRepository.findByKeyword(keyword, pageable);
    }

    public List<Crew> find9CrewList() {
        return crewRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true,false);
    }

    public List<Crew> findByAccountCrewList(Set<Tag> tags, Set<Zone> zones) {
        return crewRepository.findByAccountCrewList(tags, zones);
    }

    public List<Crew> findMemberOf(Account account) {
        return crewRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false);
    }

    public List<Crew> findManagerOf(Account account) {
        return crewRepository.findFirst5ByManagerContainingAndClosedOrderByPublishedDateTimeDesc(account, false);
    }
}
