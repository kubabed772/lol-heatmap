package pl.jbedlinski.heatmap.importing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.jbedlinski.heatmap.match.MatchImportService;
import pl.jbedlinski.heatmap.match.MatchRepository;
import pl.jbedlinski.heatmap.riot.RateLimitException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Service
public class ImportJobService {

    private final MatchImportService matchImportService;
    private final MatchRepository matchRepository;
    private final Map<String, ImportJob> jobs = new ConcurrentHashMap<>();

    public ImportJobService(MatchImportService matchImportService, MatchRepository matchRepository) {
        this.matchImportService = matchImportService;
        this.matchRepository = matchRepository;
    }

    public ImportJob getOrCreate(String puuid){
        return jobs.compute(puuid, (key, old) ->
                old != null && old.isActive() ? old : new ImportJob(puuid));
    }

    public ImportJob get(String puuid) {
        return jobs.get(puuid);
    }

    @Async
    public void run(ImportJob job, int count) {
        try {
            job.setState(ImportState.RUNNING);

            List<String> ids = withRetry(job, () -> matchImportService.fetchMatchIds(job.getPuuid(), count));
            List<String> missing = ids.stream()
                    .filter(id -> !matchRepository.existsById(id))
                    .toList();

            job.setTotal(missing.size());
            log.info("Import {}: {} new matches of {}", job.getPuuid(), missing.size(), ids.size());

            for (String matchId : missing) {
                try {
                    withRetry(job, () -> {
                        matchImportService.importMatch(matchId);
                        return null;
                    });
                } catch (RuntimeException e) {
                    log.warn("Skipping match {}: {}", matchId, e.getMessage());
                }
                job.incrementDone();
            }

            job.setState(ImportState.DONE);
        } catch (Exception e) {
            log.error("Import failed for {}", job.getPuuid(), e);
            job.setState(ImportState.FAILED);
        }
    }

    private <T> T withRetry(ImportJob job, Supplier<T> action) throws InterruptedException {
        while (true) {
            try {
                return action.get();
            } catch (RateLimitException e) {
                log.info("Rate limit, waiting {} s", e.getRetryAfter());
                job.waitFor(e.getRetryAfter());
                Thread.sleep(e.getRetryAfter() * 1000);
                job.setState(ImportState.RUNNING);
            }
        }
    }
}
