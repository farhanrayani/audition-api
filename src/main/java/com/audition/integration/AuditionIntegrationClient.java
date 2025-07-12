package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuditionIntegrationClient {

    private static final Logger LOG = LoggerFactory.getLogger(AuditionIntegrationClient.class);
    private static final String POSTS_URL = "https://jsonplaceholder.typicode.com/posts";
    private static final String POST_BY_ID_URL = "https://jsonplaceholder.typicode.com/posts/{id}";
    private static final String COMMENTS_BY_POST_URL = "https://jsonplaceholder.typicode.com/posts/{postId}/comments";
    private static final String COMMENTS_BY_POST_ID_URL = "https://jsonplaceholder.typicode.com/comments?postId={postId}";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AuditionLogger auditionLogger;

    public List<AuditionPost> getPosts() {
        try {
            auditionLogger.info(LOG, "Fetching all posts from {}", POSTS_URL);
            AuditionPost[] posts = restTemplate.getForObject(POSTS_URL, AuditionPost[].class);
            return Arrays.asList(posts != null ? posts : new AuditionPost[0]);
        } catch (final Exception e) {
            auditionLogger.logErrorWithException(LOG, "Error fetching posts", e);
            throw new SystemException("Failed to fetch posts", "External Service Error", 500, e);
        }
    }

    public AuditionPost getPostById(final String id) {
        try {
            auditionLogger.info(LOG, "Fetching post with id: {}", id);
            return restTemplate.getForObject(POST_BY_ID_URL, AuditionPost.class, id);
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

    public AuditionPost getPostByIdWithComments(final String id) {
        try {
            auditionLogger.info(LOG, "Fetching post with id: {} including comments", id);

            // First get the post
            AuditionPost post = getPostById(id);

            // Then get the comments for this post
            List<AuditionComment> comments = getCommentsForPost(id);
            post.setComments(comments);

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

    public List<AuditionComment> getCommentsForPost(final String postId) {
        try {
            auditionLogger.info(LOG, "Fetching comments for post id: {}", postId);
            AuditionComment[] comments = restTemplate.getForObject(COMMENTS_BY_POST_URL,
                    AuditionComment[].class, postId);
            return Arrays.asList(comments != null ? comments : new AuditionComment[0]);
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

    public List<AuditionComment> getCommentsByPostId(final String postId) {
        try {
            auditionLogger.info(LOG, "Fetching comments by post id: {}", postId);
            AuditionComment[] comments = restTemplate.getForObject(COMMENTS_BY_POST_ID_URL,
                    AuditionComment[].class, postId);
            return Arrays.asList(comments != null ? comments : new AuditionComment[0]);
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
}