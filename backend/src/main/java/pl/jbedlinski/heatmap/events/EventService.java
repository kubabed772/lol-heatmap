package pl.jbedlinski.heatmap.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.jbedlinski.heatmap.match.*;
import pl.jbedlinski.heatmap.riot.RiotClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventService {

    private final RiotClient riotClient;
    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final KillEventRepository killEventRepository;

    public EventService(RiotClient riotClient,
                        MatchRepository matchRepository,
                        ParticipantRepository participantRepository,
                        KillEventRepository killEventRepository) {
        this.riotClient = riotClient;
        this.matchRepository = matchRepository;
        this.participantRepository = participantRepository;
        this.killEventRepository = killEventRepository;
    }

    public List<HeatmapPoint> getPoints(String name, String tag, int count) {
        String puuid = riotClient.getAccount(name, tag).puuid();

        Map<String, ParticipantEntity> meByMatch = participantRepository.findByPuuid(puuid).stream()
                .collect(Collectors.toMap(ParticipantEntity::getMatchId, p -> p));

        List<MatchEntity> matches = matchRepository.findAllById(meByMatch.keySet()).stream()
                .filter(m -> m.getMapId() == 11)
                .sorted(Comparator.comparingLong(MatchEntity::getGameStartTimestamp).reversed())
                .limit(count)
                .toList();

        List<HeatmapPoint> result = new ArrayList<>();

        for (MatchEntity match : matches) {
            ParticipantEntity me = meByMatch.get(match.getId());
            int myId = me.getParticipantId();

            for (KillEventEntity e : killEventRepository.findByMatchIdOrderByTimestamp(match.getId())) {
                EventType type = resolveType(e, myId);
                if (type == null) continue;

                result.add(new HeatmapPoint(
                        match.getId(),
                        me.getChampionName(),
                        type,
                        e.getX(),
                        e.getY(),
                        e.getTimestamp()
                ));
            }
        }

        return result;
    }

    private EventType resolveType(KillEventEntity e, int myId) {
        if (e.getKillerId() == myId) {
            return EventType.KILL;
        }
        if (e.getVictimId() == myId) {
            return EventType.DEATH;
        }
        if (Arrays.asList(e.getAssistIds()).contains(myId)) {
            return EventType.ASSIST;
        }
        return null;
    }
}