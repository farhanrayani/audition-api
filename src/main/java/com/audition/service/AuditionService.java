package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for audition application business logic.
 *
 * This service provides cached operations for posts and comments,
 * integrating with external APIs through the AuditionIntegrationClient.
 *
 * Features include:
 * - Caching with automatic eviction
 * - Metrics collection for monitoring
 * - Filtering capabilities for posts
 * - Performance timing measurements
 *
 * @author Farhan Rayani
 * @see AuditionIntegrationClient
 */

@Service
public class AuditionService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditionService.class);

    @Autowired
    private AuditionIntegrationClient auditionIntegrationClient;

    private final Counter postsRequestCounter;
    private final Counter commentsRequestCounter;

    public AuditionService(MeterRegistry meterRegistry) {
        this.postsRequestCounter = Counter.builder("audition.posts.requests")
                .description("Number of posts requests")
                .tag("type", "all")
                .register(meterRegistry);

        this.commentsRequestCounter = Counter.builder("audition.comments.requests")
                .description("Number of comments requests")
                .register(meterRegistry);
    }

    @Cacheable(value = "posts", key = "'all-posts'", unless = "#result.isEmpty()")
    @Timed(value = "audition.posts.fetch.time", description = "Time taken to fetch posts")
    @Counted(value = "audition.posts.fetch.count", description = "Number of posts fetch operations")
    public List<AuditionPost> getPosts() {
        LOG.info("Fetching all posts from external service");
        postsRequestCounter.increment();
        return auditionIntegrationClient.getPosts();
    }

    /**
     * Filters posts based on provided criteria.
     *
     * This method performs in-memory filtering of posts retrieved from the cache.
     * Filtering logic:
     * - User ID: Exact match filtering
     * - Title: Case-insensitive substring matching
     * - Invalid user ID strings return empty results
     *
     * @param userIdFilter string representation of user ID to filter by (null/blank for no filter)
     * @param titleFilter title substring to search for (null/blank for no filter)
     * @return filtered list of posts matching all provided criteria
     * @throws NumberFormatException if userIdFilter is not a valid integer (handled gracefully)
     * @implNote This method calls getPosts() which utilizes caching for performance.
     *           Consider the cache warming strategy for production deployments.
     */

    @Timed(value = "audition.posts.filter.time", description = "Time taken to filter posts")
    public List<AuditionPost> getPostsWithFilter(final String userIdFilter, final String titleFilter) {
        LOG.info("Fetching posts with filters - userId: {}, title: {}", userIdFilter, titleFilter);

        List<AuditionPost> posts = getPosts(); // This will use cache if available

        return posts.stream()
                .filter(post -> {
                    boolean matches = true;

                    if (StringUtils.isNotBlank(userIdFilter)) {
                        try {
                            int userId = Integer.parseInt(userIdFilter);
                            matches = matches && post.getUserId() == userId;
                        } catch (NumberFormatException e) {
                            LOG.warn("Invalid userId filter provided: {}", userIdFilter);
                            // If userIdFilter is not a valid integer, no posts will match
                            return false;
                        }
                    }

                    if (StringUtils.isNotBlank(titleFilter)) {
                        matches = matches && post.getTitle() != null &&
                                post.getTitle().toLowerCase().contains(titleFilter.toLowerCase());
                    }

                    return matches;
                })
                .collect(Collectors.toList());
    }

    @Cacheable(value = "posts", key = "#postId", unless = "#result == null")
    @Timed(value = "audition.post.fetch.time", description = "Time taken to fetch single post")
    @Counted(value = "audition.post.fetch.count", description = "Number of single post fetch operations")
    public AuditionPost getPostById(final String postId) {
        LOG.info("Fetching post with id: {}", postId);
        return auditionIntegrationClient.getPostById(postId);
    }

    @Cacheable(value = "posts-with-comments", key = "#postId", unless = "#result == null")
    @Timed(value = "audition.post.with.comments.fetch.time", description = "Time taken to fetch post with comments")
    public AuditionPost getPostByIdWithComments(final String postId) {
        LOG.info("Fetching post with id: {} including comments", postId);
        return auditionIntegrationClient.getPostByIdWithComments(postId);
    }

    @Cacheable(value = "comments", key = "#postId", unless = "#result.isEmpty()")
    @Timed(value = "audition.comments.fetch.time", description = "Time taken to fetch comments")
    @Counted(value = "audition.comments.fetch.count", description = "Number of comments fetch operations")
    public List<AuditionComment> getCommentsForPost(final String postId) {
        LOG.info("Fetching comments for post id: {}", postId);
        commentsRequestCounter.increment();
        return auditionIntegrationClient.getCommentsByPostId(postId);
    }

    @CacheEvict(value = {"posts", "posts-with-comments", "comments"}, allEntries = true)
    @Scheduled(fixedDelay = 300000) // Clear cache every 5 minutes
    public void clearCache() {
        LOG.info("Clearing all caches");
    }

    @CacheEvict(value = "posts", key = "#postId")
    public void evictPostCache(final String postId) {
        LOG.info("Evicting cache for post id: {}", postId);
    }

    @CacheEvict(value = {"posts", "posts-with-comments"}, allEntries = true)
    public void evictAllPostsCache() {
        LOG.info("Evicting all posts cache");
    }
}