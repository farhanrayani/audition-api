package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Integration client for external JSONPlaceholder API.
 *
 * This client provides resilient communication with the JSONPlaceholder
 * service, implementing the following patterns:
 * - Circuit Breaker for fault tolerance
 * - Retry logic with exponential backoff
 * - Timeout management
 * - Fallback methods for graceful degradation
 *
 * All methods include comprehensive error handling and structured logging
 * for observability.
 *
 * @author Farhan Rayani
 */
@Component
public class AuditionIntegrationClient {

    private static final Logger LOG = LoggerFactory.getLogger(AuditionIntegrationClient.class);

    @Value("${audition.external-apis.jsonplaceholder.base-url:https://jsonplaceholder.typicode.com}")
    private String baseUrl;

    private static final String POSTS_ENDPOINT = "/posts";
    private static final String POST_BY_ID_ENDPOINT = "/posts/{id}";
    private static final String COMMENTS_BY_POST_ENDPOINT = "/posts/{postId}/comments";
    private static final String COMMENTS_BY_POST_ID_ENDPOINT = "/comments?postId={postId}";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AuditionLogger auditionLogger;

    @CircuitBreaker(name = "jsonplaceholder", fallbackMethod = "getPostsFallback")
    @Retry(name = "jsonplaceholder")
    @TimeLimiter(name = "jsonplaceholder")
    public CompletableFuture<List<AuditionPost>> getPostsAsync() {
        return CompletableFuture.supplyAsync(this::getPosts);
    }

    @CircuitBreaker(name = "jsonplaceholder", fallbackMethod = "getPostsFallback")
    @Retry(name = "jsonplaceholder")
    public List<AuditionPost> getPosts() {
        try {
            final String url = baseUrl + POSTS_ENDPOINT;
            auditionLogger.info(LOG, "Fetching all posts from {}", url);

            AuditionPost[] posts = restTemplate.getForObject(url, AuditionPost[].class);
            List<AuditionPost> result = Arrays.asList(posts != null ? posts : new AuditionPost[0]);

            auditionLogger.info(LOG, "Successfully fetched {} posts", result.size());
            return result;
        } catch (final Exception e) {
            auditionLogger.logErrorWithException(LOG, "Error fetching posts", e);
            throw new SystemException("Failed to fetch posts", "External Service Error", 500, e);
        }
    }

    @CircuitBreaker(name = "jsonplaceholder", fallbackMethod = "getPostByIdFallback")
    @Retry(name = "jsonplaceholder")
    public AuditionPost getPostById(final String id) {
        try {
            final String url = baseUrl + POST_BY_ID_ENDPOINT;
            auditionLogger.info(LOG, "Fetching post with id: {} from {}", id, url);

            AuditionPost result = restTemplate.getForObject(url, AuditionPost.class, id);
            auditionLogger.info(LOG, "Successfully fetched post with id: {}", id);
            return result;
        } catch (final HttpClientErrorException e) {
            auditionLogger.logHttpStatusCodeError(LOG, "Error fetching post with id: " + id, e.getStatusCode().value());

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find a Post with id " + id, "Resource Not Found", 404);
            } else {
                throw new SystemException("Failed to fetch post with id: " + id,
                        "External Service Error", e.getStatusCode().value(), e);
            }
        } catch (final Exception e) {
            auditionLogger.logErrorWithException(LOG, "Unexpected error fetching post with id: " + id, e);
            throw new SystemException("Unexpected error occurred while fetching post",
                    "Internal Server Error", 500, e);
        }
    }

    @CircuitBreaker(name = "jsonplaceholder", fallbackMethod = "getPostByIdWithCommentsFallback")
    @Retry(name = "jsonplaceholder")
    public AuditionPost getPostByIdWithComments(final String id) {
        try {
            auditionLogger.info(LOG, "Fetching post with id: {} including comments", id);

            // First get the post
            AuditionPost post = getPostById(id);

            // Then get the comments for this post
            List<AuditionComment> comments = getCommentsForPost(id);
            post.setComments(comments);

            auditionLogger.info(LOG, "Successfully fetched post with {} comments", comments.size());
            return post;
        } catch (final SystemException e) {
            // Re-throw SystemException as-is
            throw e;
        } catch (final Exception e) {
            auditionLogger.logErrorWithException(LOG, "Error fetching post with comments for id: " + id, e);
            throw new SystemException("Failed to fetch post with comments",
                    "External Service Error", 500, e);
        }
    }

    @CircuitBreaker(name = "jsonplaceholder", fallbackMethod = "getCommentsForPostFallback")
    @Retry(name = "jsonplaceholder")
    public List<AuditionComment> getCommentsForPost(final String postId) {
        try {
            final String url = baseUrl + COMMENTS_BY_POST_ENDPOINT;
            auditionLogger.info(LOG, "Fetching comments for post id: {} from {}", postId, url);

            AuditionComment[] comments = restTemplate.getForObject(url, AuditionComment[].class, postId);
            List<AuditionComment> result = Arrays.asList(comments != null ? comments : new AuditionComment[0]);

            auditionLogger.info(LOG, "Successfully fetched {} comments for post id: {}", result.size(), postId);
            return result;
        } catch (final HttpClientErrorException e) {
            auditionLogger.logHttpStatusCodeError(LOG, "Error fetching comments for post id: " + postId,
                    e.getStatusCode().value());

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find comments for Post with id " + postId,
                        "Resource Not Found", 404);
            } else {
                throw new SystemException("Failed to fetch comments for post id: " + postId,
                        "External Service Error", e.getStatusCode().value(), e);
            }
        } catch (final Exception e) {
            auditionLogger.logErrorWithException(LOG, "Unexpected error fetching comments for post id: " + postId, e);
            throw new SystemException("Unexpected error occurred while fetching comments",
                    "Internal Server Error", 500, e);
        }
    }

    @CircuitBreaker(name = "jsonplaceholder", fallbackMethod = "getCommentsByPostIdFallback")
    @Retry(name = "jsonplaceholder")
    public List<AuditionComment> getCommentsByPostId(final String postId) {
        try {
            final String url = baseUrl + COMMENTS_BY_POST_ID_ENDPOINT;
            auditionLogger.info(LOG, "Fetching comments by post id: {} from {}", postId, url);

            AuditionComment[] comments = restTemplate.getForObject(url, AuditionComment[].class, postId);
            List<AuditionComment> result = Arrays.asList(comments != null ? comments : new AuditionComment[0]);

            auditionLogger.info(LOG, "Successfully fetched {} comments by post id: {}", result.size(), postId);
            return result;
        } catch (final HttpClientErrorException e) {
            auditionLogger.logHttpStatusCodeError(LOG, "Error fetching comments by post id: " + postId,
                    e.getStatusCode().value());

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find comments for Post with id " + postId,
                        "Resource Not Found", 404);
            } else {
                throw new SystemException("Failed to fetch comments by post id: " + postId,
                        "External Service Error", e.getStatusCode().value(), e);
            }
        } catch (final Exception e) {
            auditionLogger.logErrorWithException(LOG, "Unexpected error fetching comments by post id: " + postId, e);
            throw new SystemException("Unexpected error occurred while fetching comments",
                    "Internal Server Error", 500, e);
        }
    }

    // Fallback methods for circuit breaker
    public List<AuditionPost> getPostsFallback(Exception ex) {
        auditionLogger.warn(LOG, "Fallback triggered for getPosts: {}", ex.getMessage());
        return Collections.emptyList();
    }

    public AuditionPost getPostByIdFallback(String id, Exception ex) {
        auditionLogger.warn(LOG, "Fallback triggered for getPostById with id {}: {}", id, ex.getMessage());
        throw new SystemException("Service temporarily unavailable for post " + id,
                "Service Unavailable", 503, ex);
    }

    public AuditionPost getPostByIdWithCommentsFallback(String id, Exception ex) {
        auditionLogger.warn(LOG, "Fallback triggered for getPostByIdWithComments with id {}: {}", id, ex.getMessage());
        throw new SystemException("Service temporarily unavailable for post with comments " + id,
                "Service Unavailable", 503, ex);
    }

    public List<AuditionComment> getCommentsForPostFallback(String postId, Exception ex) {
        auditionLogger.warn(LOG, "Fallback triggered for getCommentsForPost with postId {}: {}", postId, ex.getMessage());
        return Collections.emptyList();
    }

    public List<AuditionComment> getCommentsByPostIdFallback(String postId, Exception ex) {
        auditionLogger.warn(LOG, "Fallback triggered for getCommentsByPostId with postId {}: {}", postId, ex.getMessage());
        return Collections.emptyList();
    }
}