package portfolio2023.runningmate.domain;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import portfolio2023.runningmate.security.UserAccount;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Crew {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account manager;

    @OneToMany(mappedBy = "crew")
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    @ColumnDefault("0")
    private int memberCount;

    public boolean isJoinable(UserAccount userAccount){
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.manager.equals(account);
    }

    public boolean isMember(UserAccount userAccount){
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount){
        return this.manager.equals(userAccount.getAccount());
    }

    public void addMemberCount(Account account) {
        this.getMembers().add(account);
        this.memberCount++;
    }

    public String getEncodedPath() {
        return URLEncoder.encode(this.title, StandardCharsets.UTF_8);
    }
}
