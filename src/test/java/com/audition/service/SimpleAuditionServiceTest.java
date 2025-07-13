package com.audition.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimpleAuditionServiceTest {

    @Mock
    private AuditionIntegrationClient auditionIntegrationClient;

    private AuditionService auditionService;
    private MeterRegistry meterRegistry;

    private AuditionPost samplePost;
    private List<AuditionPost> samplePosts;
    private List<AuditionComment> sampleComments;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        auditionService = new AuditionService(meterRegistry);

        // Inject the mock using reflection
        try {
            java.lang.reflect.Field field = AuditionService.class.getDeclaredField("auditionIntegrationClient");
            field.setAccessible(true);
            field.set(auditionService, auditionIntegrationClient);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock", e);
        }

        // Setup test data
        samplePost = new AuditionPost();
        samplePost.setId(1);
        samplePost.setUserId(1);
        samplePost.setTitle("Sample Post");
        samplePost.setBody("This is a sample post");

        AuditionPost post2 = new AuditionPost();
        post2.setId(2);
        post2.setUserId(2);
        post2.setTitle("Another Post");
        post2.setBody("This is another post");

        AuditionPost post3 = new AuditionPost();
        post3.setId(3);
        post3.setUserId(1);
        post3.setTitle("Third Post by User 1");
        post3.setBody("This is a third post");

        samplePosts = Arrays.asList(samplePost, post2, post3);

        AuditionComment comment1 = new AuditionComment();
        comment1.setId(1);
        comment1.setPostId(1);
        comment1.setName("John Doe");
        comment1.setEmail("john@example.com");
        comment1.setBody("Great post!");

        AuditionComment comment2 = new AuditionComment();
        comment2.setId(2);
        comment2.setPostId(1);
        comment2.setName("Jane Smith");
        comment2.setEmail("jane@example.com");
        comment2.setBody("Very informative!");

        sampleComments = Arrays.asList(comment1, comment2);
    }

    @Test
    void testGetPosts() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPosts();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Sample Post", result.get(0).getTitle());
        assertEquals("Another Post", result.get(1).getTitle());
        assertEquals("Third Post by User 1", result.get(2).getTitle());

        // Verify counter was incremented
        Counter counter = meterRegistry.find("audition.posts.requests").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count(), 0.001);
    }

    @Test
    void testGetPostsWithUserIdFilter() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPostsWithFilter("1", null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(post -> post.getUserId() == 1));
        assertEquals("Sample Post", result.get(0).getTitle());
        assertEquals("Third Post by User 1", result.get(1).getTitle());
    }

    @Test
    void testGetPostsWithTitleFilter() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPostsWithFilter(null, "sample");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sample Post", result.get(0).getTitle());
    }

    @Test
    void testGetPostsWithTitleFilterCaseInsensitive() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPostsWithFilter(null, "ANOTHER");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Another Post", result.get(0).getTitle());
    }

    @Test
    void testGetPostsWithBothFilters() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPostsWithFilter("1", "third");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Third Post by User 1", result.get(0).getTitle());
        assertEquals(1, result.get(0).getUserId());
    }

    @Test
    void testGetPostsWithInvalidUserIdFilter() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPostsWithFilter("invalid", null);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetPostsWithNonExistentUserIdFilter() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPostsWithFilter("999", null);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetPostsWithNonExistentTitleFilter() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPostsWithFilter(null, "nonexistent");

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetPostsWithEmptyFilters() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPostsWithFilter("", "");

        // Then
        assertNotNull(result);
        assertEquals(3, result.size()); // Empty strings should be treated as no filter
    }

    @Test
    void testGetPostsWithWhitespaceFilters() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPostsWithFilter("  ", "  ");

        // Then
        assertNotNull(result);
        assertEquals(3, result.size()); // Whitespace should be treated as no filter
    }

    @Test
    void testGetPostsWithNullTitleInPost() {
        // Given
        AuditionPost postWithNullTitle = new AuditionPost();
        postWithNullTitle.setId(4);
        postWithNullTitle.setUserId(1);
        postWithNullTitle.setTitle(null);
        postWithNullTitle.setBody("Post with null title");

        List<AuditionPost> postsWithNullTitle = Arrays.asList(samplePost, postWithNullTitle);
        when(auditionIntegrationClient.getPosts()).thenReturn(postsWithNullTitle);

        // When
        List<AuditionPost> result = auditionService.getPostsWithFilter(null, "sample");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sample Post", result.get(0).getTitle());
    }

    @Test
    void testGetPostById() {
        // Given
        when(auditionIntegrationClient.getPostById(anyString())).thenReturn(samplePost);

        // When
        AuditionPost result = auditionService.getPostById("1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Sample Post", result.getTitle());
    }

    @Test
    void testGetPostByIdWithComments() {
        // Given
        samplePost.setComments(sampleComments);
        when(auditionIntegrationClient.getPostByIdWithComments(anyString())).thenReturn(samplePost);

        // When
        AuditionPost result = auditionService.getPostByIdWithComments("1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result.getComments());
        assertEquals(2, result.getComments().size());
    }

    @Test
    void testGetCommentsForPost() {
        // Given
        when(auditionIntegrationClient.getCommentsByPostId(anyString())).thenReturn(sampleComments);

        // When
        List<AuditionComment> result = auditionService.getCommentsForPost("1");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Great post!", result.get(0).getBody());
        assertEquals("Very informative!", result.get(1).getBody());

        // Verify counter was incremented
        Counter counter = meterRegistry.find("audition.comments.requests").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count(), 0.001);
    }

    @Test
    void testGetCommentsForPostEmpty() {
        // Given
        when(auditionIntegrationClient.getCommentsByPostId(anyString())).thenReturn(Collections.emptyList());

        // When
        List<AuditionComment> result = auditionService.getCommentsForPost("999");

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testClearCache() {
        // When
        auditionService.clearCache();

        // Then - method should execute without throwing exceptions
        // This is a simple test to ensure the method doesn't fail
        // In a real scenario, you might want to verify cache interactions
    }

    @Test
    void testEvictPostCache() {
        // When
        auditionService.evictPostCache("1");

        // Then - method should execute without throwing exceptions
    }

    @Test
    void testEvictAllPostsCache() {
        // When
        auditionService.evictAllPostsCache();

        // Then - method should execute without throwing exceptions
    }

    @Test
    void testMetricsAreRecorded() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);
        when(auditionIntegrationClient.getCommentsByPostId(anyString())).thenReturn(sampleComments);

        // When
        auditionService.getPosts();
        auditionService.getPosts(); // Call twice to verify counter increments
        auditionService.getCommentsForPost("1");

        // Then
        Counter postsCounter = meterRegistry.find("audition.posts.requests").counter();
        Counter commentsCounter = meterRegistry.find("audition.comments.requests").counter();

        assertNotNull(postsCounter);
        assertNotNull(commentsCounter);
        assertEquals(2.0, postsCounter.count(), 0.001);
        assertEquals(1.0, commentsCounter.count(), 0.001);
    }

    @Test
    void testGetPostsWithFilterEdgeCases() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // Test with zero as userId
        List<AuditionPost> result1 = auditionService.getPostsWithFilter("0", null);
        assertEquals(0, result1.size());

        // Test with negative userId
        List<AuditionPost> result2 = auditionService.getPostsWithFilter("-1", null);
        assertEquals(0, result2.size());

        // Test with very large userId
        List<AuditionPost> result3 = auditionService.getPostsWithFilter("999999", null);
        assertEquals(0, result3.size());
    }

    @Test
    void testServiceConstructorCreatesMetrics() {
        // Given
        MeterRegistry testRegistry = new SimpleMeterRegistry();

        // When
        AuditionService testService = new AuditionService(testRegistry);

        // Then
        assertNotNull(testRegistry.find("audition.posts.requests").counter());
        assertNotNull(testRegistry.find("audition.comments.requests").counter());
    }
}