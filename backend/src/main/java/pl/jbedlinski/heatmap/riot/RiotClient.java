package pl.jbedlinski.heatmap.riot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class RiotClient {
    private final RestClient rest;

    public RiotClient(@Value("${riot.api-key}") String apiKey,
                      @Value("${riot.region}") String region) {
        this.rest = RestClient.builder()
                .baseUrl("https://" + region + ".api.riotgames.com")
                .defaultHeader("X-Riot-Token", apiKey)
                .defaultStatusHandler(
                        status -> status.value() == 429,
                        (request, response) -> {
                            String header = response.getHeaders().getFirst("Retry-After");
                            long seconds = header != null ? Long.parseLong(header) : 10;
                            throw new RateLimitException(seconds);
                        })
                .build();
    }

    public AccountDto getAccount(String name, String tag) {
        return rest.get()
                .uri("/riot/account/v1/accounts/by-riot-id/{name}/{tag}", name, tag)
                .retrieve()
                .body(AccountDto.class);
    }

    public List<String> getMatchIds(String puuid, int start, int count) {
        return rest.get()
                .uri("/lol/match/v5/matches/by-puuid/{puuid}/ids?start={start}&count={count}",
                        puuid, start, count)
                .retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {});
    }

    public MatchDto getMatch(String matchId) {
        return rest.get()
                .uri("/lol/match/v5/matches/{matchId}", matchId)
                .retrieve()
                .body(MatchDto.class);
    }

    public TimelineDto getTimeline(String matchId){
        return rest.get()
                .uri("/lol/match/v5/matches/{matchId}/timeline", matchId)
                .retrieve()
                .body(TimelineDto.class);
    }
}
