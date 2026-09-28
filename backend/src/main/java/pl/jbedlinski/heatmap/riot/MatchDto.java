package pl.jbedlinski.heatmap.riot;

import java.util.List;

public record MatchDto(Info info) {

    public record Info(int mapId,
                       int queueId,
                       String gameMode,
                       long gameStartTimestamp,
                       List<Participant> participants) {}

    public record Participant(int participantId,
                              String puuid,
                              String championName,
                              int teamId) {}
}
