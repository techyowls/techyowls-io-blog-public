package io.techyowls.webflux.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * WebClient examples - the reactive HTTP client.
 *
 * WebClient replaces RestTemplate in reactive applications.
 * It's non-blocking and integrates with Project Reactor.
 */
@Component
@Slf4j
public class ExternalApiClient {

    private final WebClient webClient;

    public ExternalApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl("https://jsonplaceholder.typicode.com")
            .build();
    }

    /**
     * Simple GET request returning a single item.
     */
    public Mono<Post> getPost(int id) {
        return webClient.get()
            .uri("/posts/{id}", id)
            .retrieve()
            .bodyToMono(Post.class)
            .doOnNext(post -> log.debug("Fetched post: {}", post.title()));
    }

    /**
     * GET request returning a stream (Flux).
     */
    public Flux<Post> getAllPosts() {
        return webClient.get()
            .uri("/posts")
            .retrieve()
            .bodyToFlux(Post.class);
    }

    /**
     * POST request with body.
     */
    public Mono<Post> createPost(String title, String body, int userId) {
        return webClient.post()
            .uri("/posts")
            .bodyValue(new CreatePostRequest(title, body, userId))
            .retrieve()
            .bodyToMono(Post.class);
    }

    /**
     * Error handling with retry.
     */
    public Mono<Post> getPostWithRetry(int id) {
        return webClient.get()
            .uri("/posts/{id}", id)
            .retrieve()
            .bodyToMono(Post.class)
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                .doBeforeRetry(signal ->
                    log.warn("Retry attempt {} due to: {}",
                        signal.totalRetries() + 1,
                        signal.failure().getMessage())))
            .timeout(Duration.ofSeconds(5))
            .onErrorResume(e -> {
                log.error("Failed to fetch post after retries: {}", e.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Parallel calls - fetch multiple resources concurrently.
     */
    public Mono<CombinedData> fetchCombinedData(int userId) {
        Mono<User> userMono = webClient.get()
            .uri("/users/{id}", userId)
            .retrieve()
            .bodyToMono(User.class);

        Flux<Post> postsFlux = webClient.get()
            .uri("/posts?userId={userId}", userId)
            .retrieve()
            .bodyToFlux(Post.class);

        // Execute both in parallel, combine results
        return Mono.zip(
            userMono,
            postsFlux.collectList()
        ).map(tuple -> new CombinedData(tuple.getT1(), tuple.getT2()));
    }

    // ========== DTOs ==========

    public record Post(int id, int userId, String title, String body) {}
    public record User(int id, String name, String email) {}
    public record CreatePostRequest(String title, String body, int userId) {}
    public record CombinedData(User user, java.util.List<Post> posts) {}
}
