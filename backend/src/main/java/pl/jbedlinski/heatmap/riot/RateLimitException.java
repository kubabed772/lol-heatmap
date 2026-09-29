package pl.jbedlinski.heatmap.riot;

import lombok.Getter;

@Getter
public class RateLimitException extends RuntimeException {

    private final long retryAfter;

    public RateLimitException(long retryAfter) {
        super("Riot rate limit, retry after " + retryAfter + " s");
        this.retryAfter = retryAfter;
    }
}