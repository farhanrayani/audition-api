package com.audition.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditionServiceTest {

    @Mock
    private AuditionIntegrationClient auditionIntegrationClient;

    private AuditionService auditionService;

    private AuditionPost samplePost;
    private List<AuditionPost> samplePosts;
    private List<AuditionComment> sampleComments;

    @BeforeEach
    void setUp() {
        // Create AuditionService with SimpleMeterRegistry for testing
        auditionService = new AuditionService(new SimpleMeterRegistry());

        // Use reflection to inject the mock auditionIntegrationClient
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

        samplePosts = Arrays.asList(samplePost, post2);

        AuditionComment comment1 = new AuditionComment();
        comment1.setId(1);
        comment1.setPostId(1);
        comment1.setName("John Doe");
        comment1.setEmail("john@example.com");
        comment1.setBody("Great post!");

        sampleComments = Arrays.asList(comment1);
    }

    @Test
    void testGetPosts() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPosts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Sample Post", result.get(0).getTitle());
    }

    @Test
    void testGetPostsWithUserIdFilter() {
        // Given
        when(auditionIntegrationClient.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionService.getPostsWithFilter("1", null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserId());
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
        assertEquals(1, result.getComments().size());
    }

    @Test
    void testGetCommentsForPost() {
        // Given
        when(auditionIntegrationClient.getCommentsByPostId(anyString())).thenReturn(sampleComments);

        // When
        List<AuditionComment> result = auditionService.getCommentsForPost("1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Great post!", result.get(0).getBody());
    }
}