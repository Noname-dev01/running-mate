package portfolio2023.runningmate.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import portfolio2023.runningmate.domain.Crew;
import portfolio2023.runningmate.domain.QAccount;
import portfolio2023.runningmate.domain.Tag;
import portfolio2023.runningmate.domain.Zone;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

import static portfolio2023.runningmate.domain.QCrew.crew;
import static portfolio2023.runningmate.domain.QTag.tag;
import static portfolio2023.runningmate.domain.QZone.zone;

public class CrewRepositoryCustomImpl extends QuerydslRepositorySupport implements CrewRepositoryCustom{

    public  CrewRepositoryCustomImpl(){ super(Crew.class);}

    @Override
    public Page<Crew> findByKeyword(String keyword, Pageable pageable) {

        JPQLQuery<Crew> query = from(crew).where(crew.published.isTrue()
                        .and(crew.title.containsIgnoreCase(keyword))
                        .or(crew.tags.any().title.containsIgnoreCase(keyword))
                        .or(crew.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(crew.tags, tag).fetchJoin()
                .leftJoin(crew.zones, zone).fetchJoin()
                .distinct();

        JPQLQuery<Crew> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Crew> fetchResults = pageableQuery.fetchResults();

        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    @Override
    public List<Crew> findByAccountCrewList(Set<Tag> tags, Set<Zone> zones) {
        return from(crew).where(crew.published.isTrue()
                .and(crew.closed.isFalse())
                .and(crew.tags.any().in(tags))
                .and(crew.zones.any().in(zones)))
                .leftJoin(crew.tags, tag).fetchJoin()
                .leftJoin(crew.zones, zone).fetchJoin()
                .orderBy(crew.publishedDateTime.desc())
                .distinct()
                .limit(9).fetch();
    }

}
