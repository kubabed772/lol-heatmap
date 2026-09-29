package pl.jbedlinski.heatmap.events;

import org.springframework.stereotype.Service;
import pl.jbedlinski.heatmap.match.*;
import pl.jbedlinski.heatmap.riot.RiotClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EventService {

    private final RiotClient riotClient;
    private final MatchImportService matchImportService;
    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final KillEventRepository killEventRepository;

    public EventService(RiotClient riotClient,
                        MatchImportService matchImportService,
                        MatchRepository matchRepository,
                        ParticipantRepository participantRepository,
                        KillEventRepository killEventRepository) {
        this.riotClient = riotClient;
        this.matchImportService = matchImportService;
        this.matchRepository = matchRepository;
        this.participantRepository = participantRepository;
        this.killEventRepository = killEventRepository;
    }

    public List<HeatmapPoint> getPoints(String name, String tag, int count) {
        String puuid = riotClient.getAccount(name, tag).puuid();
        List<String> matchIds = riotClient.getMatchIds(puuid, count);

        List<HeatmapPoint> result = new ArrayList<>();

        for (String matchId : matchIds) {
            if (!matchRepository.existsById(matchId)) {
                matchImportService.importMatch(matchId);
            }

            MatchEntity match = matchRepository.findById(matchId).orElseThrow();
            if (match.getMapId() != 11) continue;

            ParticipantEntity me = participantRepository.findByMatchIdAndPuuid(matchId, puuid).orElseThrow();
            int myId = me.getParticipantId();

            for (KillEventEntity e : killEventRepository.findByMatchIdOrderByTimestamp(matchId)) {
                EventType type = resolveType(e, myId);
                if (type == null) continue;

                result.add(new HeatmapPoint(
                        matchId,
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