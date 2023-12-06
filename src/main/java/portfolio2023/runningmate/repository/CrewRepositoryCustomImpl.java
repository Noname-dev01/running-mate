package portfolio2023.runningmate.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import portfolio2023.runningmate.domain.Crew;

import javax.persistence.EntityManager;
import java.util.List;

import static portfolio2023.runningmate.domain.QCrew.crew;

public class CrewRepositoryCustomImpl implements CrewRepositoryCustom{

    private final JPAQueryFactory query;

    public CrewRepositoryCustomImpl(EntityManager entityManager) {
        this.query = new JPAQueryFactory(entityManager);
    }


    @Override
    public Page<Crew> findByKeyword(String keyword, Pageable pageable) {

        List<Crew> result = query
                .select(crew)
                .from(crew).where(crew.published.isTrue()
                        .and(crew.title.containsIgnoreCase(keyword))
                        .or(crew.tags.any().title.containsIgnoreCase(keyword))
                        .or(crew.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .fetch();

        JPAQuery<Long> countQuery = query.select(crew.count())
                .from(crew)
                .where(crew.published.isTrue()
                        .and(crew.title.containsIgnoreCase(keyword))
                        .or(crew.tags.any().title.containsIgnoreCase(keyword))
                        .or(crew.zones.any().localNameOfCity.containsIgnoreCase(keyword)));

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }
}
