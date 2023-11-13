package portfolio2023.runningmate.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Tag {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;


}
