package pl.jbedlinski.heatmap.importing;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class ImportJob {

    private final String puuid;
    private volatile ImportState state = ImportState.QUEUED;
    private volatile int total;
    private final AtomicInteger done = new AtomicInteger();
    private volatile long waitingUntil;

    public ImportJob(String puuid) {
        this.puuid = puuid;
    }

    public boolean isActive() {
        return state == ImportState.QUEUED || state == ImportState.RUNNING || state == ImportState.WAITING;
    }

    public void setState(ImportState state) { this.state = state; }
    public void setTotal(int total) { this.total = total; }
    public void incrementDone() { done.incrementAndGet(); }

    public void waitFor(long seconds) {
        this.state = ImportState.WAITING;
        this.waitingUntil = System.currentTimeMillis() + seconds * 1000;
    }

    public ImportStatus toStatus() {
        long retryIn = state == ImportState.WAITING
                ? Math.max(0, (waitingUntil - System.currentTimeMillis()) / 1000)
                : 0;
        return new ImportStatus(puuid, state, total, done.get(), retryIn);
    }
}
