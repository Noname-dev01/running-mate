package portfolio2023.runningmate.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.QAccount;
import portfolio2023.runningmate.domain.QTag;
import portfolio2023.runningmate.domain.QZone;

import javax.persistence.EntityManager;
import java.util.List;

import static portfolio2023.runningmate.domain.QCrew.crew;
import static portfolio2023.runningmate.domain.QTag.*;
import static portfolio2023.runningmate.domain.QZone.zone;

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
                .leftJoin(crew.tags, tag).fetchJoin()
                .leftJoin(crew.zones, zone).fetchJoin()
                .leftJoin(crew.members, QAccount.account).fetchJoin()
                .distinct()
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
