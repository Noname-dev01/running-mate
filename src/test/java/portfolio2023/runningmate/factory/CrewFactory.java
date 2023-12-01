package portfolio2023.runningmate.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.repository.CrewRepository;
import portfolio2023.runningmate.service.CrewService;

@Component
public class CrewFactory {

    @Autowired CrewService crewService;
    @Autowired CrewRepository crewRepository;

    public Crew createCrew(String title, Account account){
        Crew crew = new Crew();
        crew.setTitle(title);
        crewService.createNewCrew(crew, account);
        return crew;
    }

}
