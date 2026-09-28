package pl.jbedlinski.heatmap.riot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RiotClient {
    private final RestClient rest;

    public RiotClient(@Value("${riot.api-key}") String apiKey,
                      @Value("${riot.region}") String region) {
        this.rest = RestClient.builder()
                .baseUrl("https://" + region + ".api.riotgames.com")
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
    }

    public AccountDto getAccount(String name, String tag) {
        return rest.get()
                .uri("/riot/account/v1/accounts/by-riot-id/{name}/{tag}", name, tag)
                .retrieve()
                .body(AccountDto.class);
    }
}
