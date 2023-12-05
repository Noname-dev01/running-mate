package portfolio2023.runningmate.repository;

import com.querydsl.core.types.Predicate;
import portfolio2023.runningmate.domain.QAccount;
import portfolio2023.runningmate.domain.Tag;
import portfolio2023.runningmate.domain.Zone;

import java.util.Set;

public class AccountPredicates {

    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones){
        QAccount account = QAccount.account;
        return account.zones.any().in(zones).and(account.tags.any().in(tags));
    }
}
