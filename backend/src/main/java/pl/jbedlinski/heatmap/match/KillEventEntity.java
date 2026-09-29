package pl.jbedlinski.heatmap.match;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kill_events")
@Getter
@Setter
@NoArgsConstructor
public class KillEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matchId;
    private long timestamp;
    private int killerId;
    private int victimId;
    private Integer[] assistIds;
    private int x;
    private int y;
}
