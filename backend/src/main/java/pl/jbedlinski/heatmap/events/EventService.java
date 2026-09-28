package pl.jbedlinski.heatmap.events;

import org.springframework.stereotype.Service;
import pl.jbedlinski.heatmap.riot.MatchDto;
import pl.jbedlinski.heatmap.riot.RiotClient;
import pl.jbedlinski.heatmap.riot.TimelineDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EventService {

    private final RiotClient riotClient;

    public EventService(RiotClient riotClient) {
        this.riotClient = riotClient;
    }

    public List<HeatmapPoint> getPoints(String name, String tag, int count){
        String puuid = riotClient.getAccount(name, tag).puuid();
        List<String> matchIds = riotClient.getMatchIds(puuid, count);

        List<HeatmapPoint> result = new ArrayList<>();

        for(String matchId : matchIds){
            MatchDto match = riotClient.getMatch(matchId);

            MatchDto.Participant me = match.info().participants().stream()
                    .filter(p -> p.puuid().equals(puuid))
                    .findFirst()
                    .orElseThrow();

            if (match.info().mapId() != 11) {
                continue;
            }

            int myId = me.participantId();
            TimelineDto timeline = riotClient.getTimeline(matchId);

            for(TimelineDto.Frame frame : timeline.info().frames()) {
                for(TimelineDto.Event e : frame.events()) {
                    if(!"CHAMPION_KILL".equals(e.type()) || e.position()==null) continue;

                    EventType type = resolveType(e, myId);

                    if (type == null) continue;

                    result.add(new HeatmapPoint(
                            matchId,
                            me.championName(),
                            type,
                            e.position().x(),
                            e.position().y(),
                            e.timestamp()
                    ));
                }
            }
        }

        return result;
    }

    private EventType resolveType(TimelineDto.Event e, int myId) {
        if (Objects.equals(e.killerId(), myId)) {
            return EventType.KILL;
        }
        if (Objects.equals(e.victimId(), myId)) {
            return EventType.DEATH;
        }
        if (e.assistingParticipantIds() != null && e.assistingParticipantIds().contains(myId)) {
            return EventType.ASSIST;
        }
        return null;
    }
}
