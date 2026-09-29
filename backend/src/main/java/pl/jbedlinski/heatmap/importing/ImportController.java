package pl.jbedlinski.heatmap.importing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jbedlinski.heatmap.riot.RiotClient;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportJobService importJobService;
    private final RiotClient riotClient;

    public ImportController(ImportJobService importJobService, RiotClient riotClient) {
        this.importJobService = importJobService;
        this.riotClient = riotClient;
    }

    @PostMapping
    public ResponseEntity<ImportStatus> start(@RequestParam String name,
                                              @RequestParam String tag,
                                              @RequestParam(defaultValue = "100") int count) {
        String puuid = riotClient.getAccount(name, tag).puuid();
        ImportJob job = importJobService.getOrCreate(puuid);

        if (job.getState() == ImportState.QUEUED) importJobService.run(job, count);

        return ResponseEntity.accepted().body(job.toStatus());
    }

    @GetMapping("/{puuid}")
    public ResponseEntity<ImportStatus> status(@PathVariable String puuid) {
        ImportJob job = importJobService.get(puuid);
        if (job == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(job.toStatus());
    }
}