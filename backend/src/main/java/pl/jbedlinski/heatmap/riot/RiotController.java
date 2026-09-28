package pl.jbedlinski.heatmap.riot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}