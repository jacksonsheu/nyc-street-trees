package com.nyctrees.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Applies a simple per-client-IP token-bucket rate limit to interaction write
 * endpoints, protecting low-tier demo deployments from scripted spam/abuse
 * (independent of the overall interaction capacity cap).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final ConcurrentMap<String, Bucket> bucketsByClient = new ConcurrentHashMap<>();
    private final int capacity;
    private final Duration refillPeriod;

    /**
     * Creates the filter with a configurable request budget per client IP.
     *
     * @param capacity     max requests a client may make per {@code refillPeriodSeconds} window,
     *                      configured via {@code app.rate-limit.capacity}
     * @param refillPeriodSeconds length of the refill window in seconds,
     *                      configured via {@code app.rate-limit.refill-period-seconds}
     */
    public RateLimitFilter(
            @Value("${app.rate-limit.capacity:20}") int capacity,
            @Value("${app.rate-limit.refill-period-seconds:60}") long refillPeriodSeconds
    ) {
        this.capacity = capacity;
        this.refillPeriod = Duration.ofSeconds(refillPeriodSeconds);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isRateLimited(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = bucketsByClient.computeIfAbsent(clientKey(request), ignored -> newBucket());
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setContentType("text/plain");
        response.getWriter().write("Too many requests. Please slow down and try again shortly.");
    }

    /**
     * Only rate-limits interaction write endpoints; reads (map browsing, detail views)
     * stay unrestricted for a smooth demo browsing experience.
     */
    private boolean isRateLimited(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/api/trees/");
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refillPeriod)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Uses the client's forwarded IP when present (typical behind a hosting provider's
     * proxy/load balancer), falling back to the direct remote address otherwise.
     */
    private String clientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
