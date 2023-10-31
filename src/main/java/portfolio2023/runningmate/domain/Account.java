package portfolio2023.runningmate.domain;

import lombok.*;
import org.hibernate.annotations.CollectionId;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime joinedAt;

    private String introduction;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean runningCreatedByEmail;

    private boolean runningCreatedByWeb;

    private boolean runningRecruitByEmail;

    private boolean runningRecruitByWeb;

    private boolean runningUpdatedByEmail;

    private boolean runningUpdatedByWeb;
}
