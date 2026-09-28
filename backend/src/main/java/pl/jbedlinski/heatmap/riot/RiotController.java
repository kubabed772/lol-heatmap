package pl.jbedlinski.heatmap.riot;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/riot")
public class RiotController {

    private final RiotClient riotClient;

    public RiotController(RiotClient riotClient) {
        this.riotClient = riotClient;
    }

    @GetMapping("/account")
    public AccountDto getAccount(@RequestParam String name,
                                 @RequestParam String tag) {
        return riotClient.getAccount(name, tag);
    }

    @GetMapping("/matches")
    public List<String> getMatchIds(@RequestParam String puuid,
                                    @RequestParam(defaultValue = "5") int count) {
        return riotClient.getMatchIds(puuid, count);
    }

    @GetMapping("/matches/{matchId}")
    public MatchDto getMatch(@PathVariable String matchId){
        return riotClient.getMatch(matchId);
    }

}