package pl.jbedlinski.heatmap.importing;

public record ImportStatus(String puuid, ImportState state, int total, int done, long retryInSeconds) {}