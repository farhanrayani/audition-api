package com.audition.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class AuditionIntegrationClientTest {

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

        sampleCommentsArray = new AuditionComment[]{comment1};
    }

    @Test
    void testGetPosts() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost[].class)))
                .thenReturn(samplePostsArray);

        // When
        List<AuditionPost> result = auditionIntegrationClient.getPosts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Sample Post", result.get(0).getTitle());
    }

    @Test
    void testGetPostsWithException() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost[].class)))
                .thenThrow(new RuntimeException("Network error"));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPosts());

        assertEquals("Failed to fetch posts", exception.getMessage());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void testGetPostById() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), anyString()))
                .thenReturn(samplePost);

        // When
        AuditionPost result = auditionIntegrationClient.getPostById("1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Sample Post", result.getTitle());
    }

    @Test
    void testGetPostByIdNotFound() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostById("999"));

        assertEquals("Cannot find a Post with id 999", exception.getMessage());
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void testGetPostByIdServerError() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When & Then
        SystemException exception = assertThrows(SystemException.class,
                () -> auditionIntegrationClient.getPostById("1"));

        assertEquals("Failed to fetch post with id: 1", exception.getMessage());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void testGetCommentsForPost() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionComment[].class), anyString()))
                .thenReturn(sampleCommentsArray);

        // When
        List<AuditionComment> result = auditionIntegrationClient.getCommentsForPost("1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Great post!", result.get(0).getBody());
    }

    @Test
    void testGetCommentsByPostId() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionComment[].class), anyString()))
                .thenReturn(sampleCommentsArray);

        // When
        List<AuditionComment> result = auditionIntegrationClient.getCommentsByPostId("1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Great post!", result.get(0).getBody());
    }

    @Test
    void testGetPostByIdWithComments() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), anyString()))
                .thenReturn(samplePost);
        when(restTemplate.getForObject(anyString(), eq(AuditionComment[].class), anyString()))
                .thenReturn(sampleCommentsArray);

        // When
        AuditionPost result = auditionIntegrationClient.getPostByIdWithComments("1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
        assertEquals("Great post!", result.getComments().get(0).getBody());
    }
}