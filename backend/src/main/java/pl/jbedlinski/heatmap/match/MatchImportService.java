package pl.jbedlinski.heatmap.match;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.jbedlinski.heatmap.riot.MatchDto;
import pl.jbedlinski.heatmap.riot.RiotClient;
import pl.jbedlinski.heatmap.riot.TimelineDto;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MatchImportService {

    private final RiotClient riotClient;
    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final KillEventRepository killEventRepository;

    public MatchImportService(RiotClient riotClient,
                              MatchRepository matchRepository,
                              ParticipantRepository participantRepository,
                              KillEventRepository killEventRepository
    ){
        this.riotClient = riotClient;
        this.matchRepository = matchRepository;
        this.participantRepository = participantRepository;
        this.killEventRepository = killEventRepository;
    }

    @Transactional
    public void importMatch(String matchId){
        MatchDto match = riotClient.getMatch(matchId);
        TimelineDto timeline = riotClient.getTimeline(matchId);

        MatchEntity m = new MatchEntity();
        m.setId(matchId);
        m.setMapId(match.info().mapId());
        m.setQueueId(match.info().queueId());
        m.setGameStartTimestamp(match.info().gameStartTimestamp());
        matchRepository.save(m);

        for(MatchDto.Participant p : match.info().participants()){
            ParticipantEntity pe = new ParticipantEntity();
            pe.setMatchId(matchId);
            pe.setParticipantId(p.participantId());
            pe.setPuuid(p.puuid());
            pe.setChampionName(p.championName());
            pe.setTeamId(p.teamId());
            participantRepository.save(pe);
        }

        for(TimelineDto.Frame frame : timeline.info().frames()){
            for(TimelineDto.Event e : frame.events()){
                if (!"CHAMPION_KILL".equals(e.type()) || e.position() == null) continue;

                KillEventEntity k = new KillEventEntity();
                k.setMatchId(matchId);
                k.setTimestamp(e.timestamp());
                k.setKillerId(e.killerId() == null ? 0 : e.killerId());
                k.setVictimId(e.victimId() == null ? 0 : e.victimId());
                k.setAssistIds(e.assistingParticipantIds()==null
                    ? new Integer[0]
                    : e.assistingParticipantIds().toArray(new Integer[0]));
                k.setX(e.position().x());
                k.setY(e.position().y());
                killEventRepository.save(k);
            }
        }
    }


    public List<String> fetchMatchIds(String puuid, int total) {
        List<String> ids = new ArrayList<>();
        int start = 0;

        while (ids.size() < total) {
            int pageSize = Math.min(100, total - ids.size());
            List<String> page = riotClient.getMatchIds(puuid, start, pageSize);
            log.info("Match ids: start={}, requested={}, got={}", start, pageSize, page.size());

            ids.addAll(page);
            if (page.size() < pageSize) break;

            start += pageSize;
        }

        return ids;
    }
}
