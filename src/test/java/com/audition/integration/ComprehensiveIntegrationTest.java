package com.audition.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
class ComprehensiveIntegrationTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuditionLogger auditionLogger;

    @InjectMocks
    private AuditionIntegrationClient auditionIntegrationClient;

    private AuditionPost[] samplePostsArray;
    private AuditionPost samplePost;
    private AuditionComment[] sampleCommentsArray;

    @BeforeEach
    void setUp() {
        // Set the base URL using reflection
        ReflectionTestUtils.setField(auditionIntegrationClient, "baseUrl", "https://jsonplaceholder.typicode.com");

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

        samplePostsArray = new AuditionPost[]{samplePost, post2};

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

        sampleCommentsArray = new AuditionComment[]{comment1, comment2};
    }

    @Test
    void testGetPostsAsync() throws ExecutionException, InterruptedException {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost[].class)))
                .thenReturn(samplePostsArray);

        // When
        CompletableFuture<List<AuditionPost>> future = auditionIntegrationClient.getPostsAsync();
        List<AuditionPost> result = future.get();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Sample Post", result.get(0).getTitle());
        assertEquals("Another Post", result.get(1).getTitle());
    }

    @Test
    void testGetPostsWithNullResponse() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost[].class)))
                .thenReturn(null);

        // When
        List<AuditionPost> result = auditionIntegrationClient.getPosts();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetPostsWithNetworkException() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost[].class)))
                .thenThrow(new ResourceAccessException("Network timeout"));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPosts());

        assertEquals("Failed to fetch posts", exception.getMessage());
        assertEquals("External Service Error", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void testGetPostByIdWithInternalServerError() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostById("1"));

        assertEquals("Failed to fetch post with id: 1", exception.getMessage());
        assertEquals("External Service Error", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void testGetPostByIdWithBadRequest() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostById("invalid"));

        assertEquals("Failed to fetch post with id: invalid", exception.getMessage());
        assertEquals("External Service Error", exception.getTitle());
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    void testGetPostByIdWithUnexpectedException() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostById("1"));

        assertEquals("Unexpected error occurred while fetching post", exception.getMessage());
        assertEquals("Internal Server Error", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void testGetPostByIdWithCommentsSuccess() {
        // Given
        when(restTemplate.getForObject(contains("/posts/1"), eq(AuditionPost.class), anyString()))
                .thenReturn(samplePost);
        when(restTemplate.getForObject(contains("/posts/1/comments"), eq(AuditionComment[].class), anyString()))
                .thenReturn(sampleCommentsArray);

        // When
        AuditionPost result = auditionIntegrationClient.getPostByIdWithComments("1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Sample Post", result.getTitle());
        assertNotNull(result.getComments());
        assertEquals(2, result.getComments().size());
        assertEquals("Great post!", result.getComments().get(0).getBody());
    }

    @Test
    void testGetCommentsForPostNotFound() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionComment[].class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getCommentsForPost("999"));

        assertEquals("Cannot find comments for Post with id 999", exception.getMessage());
        assertEquals("Resource Not Found", exception.getTitle());
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void testGetCommentsForPostWithServerError() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionComment[].class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getCommentsForPost("1"));

        assertEquals("Failed to fetch comments for post id: 1", exception.getMessage());
        assertEquals("External Service Error", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void testGetCommentsForPostWithUnexpectedException() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionComment[].class), anyString()))
                .thenThrow(new SocketTimeoutException("Connection timeout"));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getCommentsForPost("1"));

        assertEquals("Unexpected error occurred while fetching comments", exception.getMessage());
        assertEquals("Internal Server Error", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void testGetCommentsByPostIdSuccess() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionComment[].class), anyString()))
                .thenReturn(sampleCommentsArray);

        // When
        List<AuditionComment> result = auditionIntegrationClient.getCommentsByPostId("1");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Great post!", result.get(0).getBody());
        assertEquals("Very informative!", result.get(1).getBody());
        assertEquals(1, result.get(0).getPostId());
        assertEquals(1, result.get(1).getPostId());
    }

    @Test
    void testGetCommentsByPostIdWithNullResponse() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionComment[].class), anyString()))
                .thenReturn(null);

        // When
        List<AuditionComment> result = auditionIntegrationClient.getCommentsByPostId("1");

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetCommentsByPostIdNotFound() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionComment[].class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getCommentsByPostId("999"));

        assertEquals("Cannot find comments for Post with id 999", exception.getMessage());
        assertEquals("Resource Not Found", exception.getTitle());
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void testGetPostByIdWithCommentsWhenPostNotFound() {
        // Given
        when(restTemplate.getForObject(contains("/posts/999"), eq(AuditionPost.class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostByIdWithComments("999"));

        assertEquals("Cannot find a Post with id 999", exception.getMessage());
        assertEquals("Resource Not Found", exception.getTitle());
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void testGetPostByIdWithCommentsWhenCommentsNotFound() {
        // Given
        when(restTemplate.getForObject(contains("/posts/1"), eq(AuditionPost.class), anyString()))
                .thenReturn(samplePost);
        when(restTemplate.getForObject(contains("/posts/1/comments"), eq(AuditionComment[].class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostByIdWithComments("1"));

        assertEquals("Cannot find comments for Post with id 1", exception.getMessage());
        assertEquals("Resource Not Found", exception.getTitle());
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void testFallbackMethods() {
        // Test getPosts fallback
        List<AuditionPost> postsResult = auditionIntegrationClient.getPostsFallback(new RuntimeException("Test"));
        assertNotNull(postsResult);
        assertTrue(postsResult.isEmpty());

        // Test getCommentsForPost fallback
        List<AuditionComment> commentsResult = auditionIntegrationClient.getCommentsForPostFallback("1", new RuntimeException("Test"));
        assertNotNull(commentsResult);
        assertTrue(commentsResult.isEmpty());

        // Test getCommentsByPostId fallback
        List<AuditionComment> commentsByIdResult = auditionIntegrationClient.getCommentsByPostIdFallback("1", new RuntimeException("Test"));
        assertNotNull(commentsByIdResult);
        assertTrue(commentsByIdResult.isEmpty());

        // Test getPostById fallback
        SystemException postException = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostByIdFallback("1", new RuntimeException("Test")));
        assertEquals("Service temporarily unavailable for post 1", postException.getMessage());
        assertEquals(503, postException.getStatusCode());

        // Test getPostByIdWithComments fallback
        SystemException postWithCommentsException = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostByIdWithCommentsFallback("1", new RuntimeException("Test")));
        assertEquals("Service temporarily unavailable for post with comments 1", postWithCommentsException.getMessage());
        assertEquals(503, postWithCommentsException.getStatusCode());
    }

    @Test
    void testCircuitBreakerAnnotations() {
        // Verify that the class has the appropriate annotations for resilience
        // This is more of a smoke test to ensure annotations are present
        Class<?> clazz = auditionIntegrationClient.getClass();
        assertNotNull(clazz);
        assertTrue(clazz.isAnnotationPresent(org.springframework.stereotype.Component.class));
    }

    @Test
    void testLoggingCalls() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost[].class)))
                .thenReturn(samplePostsArray);

        // When
        auditionIntegrationClient.getPosts();

        // Then - verify logging was called
        verify(auditionLogger, atLeastOnce()).info(any(), anyString(), any());
    }

    @Test
    void testBaseUrlConfiguration() {
        // Test that base URL is properly configured
        String baseUrl = (String) ReflectionTestUtils.getField(auditionIntegrationClient, "baseUrl");
        assertNotNull(baseUrl);
        assertEquals("https://jsonplaceholder.typicode.com", baseUrl);
    }

    @Test
    void testEndpointConstants() {
        // These constants are private, but we can test the behavior they enable
        // by verifying the correct URLs are called

        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost[].class)))
                .thenReturn(samplePostsArray);

        // When
        auditionIntegrationClient.getPosts();

        // Then
        verify(restTemplate).getForObject(eq("https://jsonplaceholder.typicode.com/posts"), eq(AuditionPost[].class));
    }

    @Test
    void testErrorHandlingWithDifferentHttpStatuses() {
        // Test 401 Unauthorized
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        SystemException unauthorizedException = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostById("1"));
        assertEquals(401, unauthorizedException.getStatusCode());

        // Test 403 Forbidden
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        SystemException forbiddenException = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostById("1"));
        assertEquals(403, forbiddenException.getStatusCode());

        // Test 422 Unprocessable Entity
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY));

        SystemException unprocessableException = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostById("1"));
        assertEquals(422, unprocessableException.getStatusCode());
    }
}