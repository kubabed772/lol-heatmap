package pl.jbedlinski.heatmap.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KillEventRepository extends JpaRepository<KillEventEntity, Long> {

    List<KillEventEntity> findByMatchIdOrderByTimestamp(String matchId);
}