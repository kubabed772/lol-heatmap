package pl.jbedlinski.heatmap.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<ParticipantEntity, Long> {

    Optional<ParticipantEntity> findByMatchIdAndPuuid(String matchId, String puuid);
}