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

    @ManyToMany
    private Set<Account> manager = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    private String bannerName;
    private String bannerPath;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

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
                && !this.members.contains(account) && !this.manager.contains(account);
    }

    public boolean isMember(UserAccount userAccount){
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount){
        return this.manager.contains(userAccount.getAccount());
    }

    public String getEncodedTitle() {
        return URLEncoder.encode(this.title, StandardCharsets.UTF_8);
    }

    public void publish() {
        if (!this.closed && !this.published){
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("크루를 공개할 수 없는 상태입니다. 크루를 이미 공개했거나 종료했습니다.");
        }
    }


    public void close() {
        if (this.published && !this.closed){
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("크루를 종료할 수 없습니다. 크루를 공개하지 않았거나 이미 종료된 크루입니다.");
        }
    }

    public void startRecruit() {
        if (canUpdateRecruiting()){
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("크루원 모집을 시작할 수 없습니다. 크루를 공개하거나 5분뒤 다시 시도하세요.");
        }
    }

    public void stopRecruit() {
        if (canUpdateRecruiting()){
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 크루를 공개하거나 5분뒤 다시 시도하세요.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdatedDateTime == null || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusMinutes(5));
    }

    public boolean isRemovable() {
        return !this.published;

    }

    public void addMemberCount(Account account) {
        this.getMembers().add(account);
        this.memberCount++;
    }

    public void removeAccount(Account account) {
        this.getMembers().remove(account);
        this.memberCount--;
    }

    public void addManager(Account account) {
        this.manager.add(account);
    }

    public boolean isManagerBy(Account account){
        return this.getManager().contains(account);
    }

    public void addMember(Account account){
        this.members.add(account);
    }
}
