package pl.jbedlinski.heatmap.match;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class MatchEntity {

    @Id
    private String id;

    private int mapId;
    private int queueId;
    private long gameStartTimestamp;
}