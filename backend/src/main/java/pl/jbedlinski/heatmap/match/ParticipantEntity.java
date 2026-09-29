package pl.jbedlinski.heatmap.match;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "participants",
        uniqueConstraints = @UniqueConstraint(name = "uk_participant_match_pid", columnNames = {"match_id", "participant_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String matchId;
    private int participantId;
    private String puuid;
    private String championName;
    private int teamId;
}
