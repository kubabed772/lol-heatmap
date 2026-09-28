package pl.jbedlinski.heatmap.events;

public record HeatmapPoint(String matchId,
                           String champion,
                           EventType type,
                           int x,
                           int y,
                           long timestamp) {}