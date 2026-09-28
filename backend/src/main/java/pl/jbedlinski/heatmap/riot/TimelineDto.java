package pl.jbedlinski.heatmap.riot;

import java.util.List;

public record TimelineDto(Info info) {

    public record Info(List<Frame> frames) {}

    public record Frame(List<Event> events) {}

    public record Event(String type,
                        long timestamp,
                        Integer killerId,
                        Integer victimId,
                        List<Integer> assistingParticipantIds,
                        Position position) {}

    public record Position(int x, int y) {}
}
