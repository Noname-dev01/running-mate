package portfolio2023.runningmate.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@NamedEntityGraph(
        name = "Enrollment.withEventAndCrew",
        attributeNodes = {
                @NamedAttributeNode(value = "event", subgraph = "crew")
        },
        subgraphs = @NamedSubgraph(name = "crew", attributeNodes = @NamedAttributeNode("crew"))
)
@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;

}
