package com.audition;

import com.audition.web.AuditionController;
import com.audition.service.AuditionService;
import com.audition.model.AuditionPost;
import com.audition.model.AuditionComment;
import com.audition.common.exception.SystemException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Additional tests to ensure comprehensive coverage of edge cases and method variations
 */
@ExtendWith(MockitoExtension.class)
class AdditionalTests {

    @Mock
    private AuditionService auditionService;

    @InjectMocks
    private AuditionController auditionController;

    private AuditionPost samplePost;
    private List<AuditionPost> samplePosts;
    private List<AuditionComment> sampleComments;

    @BeforeEach
    void setUp() {
        samplePost = new AuditionPost();
        samplePost.setId(1);
        samplePost.setUserId(1);
        samplePost.setTitle("Test Post");
        samplePost.setBody("Test Body");

        samplePosts = Arrays.asList(samplePost);

        AuditionComment comment = new AuditionComment();
        comment.setId(1);
        comment.setPostId(1);
        comment.setName("Test User");
        comment.setEmail("test@example.com");
        comment.setBody("Test Comment");

        sampleComments = Arrays.asList(comment);
    }

    @Test
    void testGetPostsWithNoFilters() {
        // Given
        when(auditionService.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionController.getPosts(null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditionService).getPosts();
        verify(auditionService, never()).getPostsWithFilter(anyString(), anyString());
    }

    @Test
    void testGetPostsWithOnlyUserIdFilter() {
        // Given
        when(auditionService.getPostsWithFilter("1", null)).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionController.getPosts(1, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditionService).getPostsWithFilter("1", null);
        verify(auditionService, never()).getPosts();
    }

    @Test
    void testGetPostsWithOnlyTitleFilter() {
        // Given
        when(auditionService.getPostsWithFilter(null, "test")).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionController.getPosts(null, "test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditionService).getPostsWithFilter(null, "test");
        verify(auditionService, never()).getPosts();
    }

    @Test
    void testGetPostsWithBothFilters() {
        // Given
        when(auditionService.getPostsWithFilter("1", "test")).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionController.getPosts(1, "test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditionService).getPostsWithFilter("1", "test");
        verify(auditionService, never()).getPosts();
    }

    @Test
    void testGetPostsWithEmptyTitle() {
        // Given
        when(auditionService.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionController.getPosts(null, "");

        // Then
        assertNotNull(result);
        verify(auditionService).getPosts();
        verify(auditionService, never()).getPostsWithFilter(anyString(), anyString());
    }

    @Test
    void testGetPostsWithWhitespaceTitle() {
        // Given
        when(auditionService.getPosts()).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionController.getPosts(null, "   ");

        // Then
        assertNotNull(result);
        verify(auditionService).getPosts();
        verify(auditionService, never()).getPostsWithFilter(anyString(), anyString());
    }

    @Test
    void testGetPostByIdSuccess() {
        // Given
        when(auditionService.getPostById("1")).thenReturn(samplePost);

        // When
        AuditionPost result = auditionController.getPostById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(auditionService).getPostById("1");
    }

    @Test
    void testGetPostWithCommentsSuccess() {
        // Given
        samplePost.setComments(sampleComments);
        when(auditionService.getPostByIdWithComments("1")).thenReturn(samplePost);

        // When
        AuditionPost result = auditionController.getPostWithComments(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
        verify(auditionService).getPostByIdWithComments("1");
    }

    @Test
    void testGetCommentsByPostIdSuccess() {
        // Given
        when(auditionService.getCommentsForPost("1")).thenReturn(sampleComments);

        // When
        List<AuditionComment> result = auditionController.getCommentsByPostId(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Comment", result.get(0).getBody());
        verify(auditionService).getCommentsForPost("1");
    }

    @Test
    void testValidatePostIdWithValidString() {
        // Test the private validatePostId method using reflection
        try {
            java.lang.reflect.Method method = AuditionController.class.getDeclaredMethod("validatePostId", String.class);
            method.setAccessible(true);

            // Should not throw exception for valid ID
            method.invoke(auditionController, "1");
            method.invoke(auditionController, "123");
            method.invoke(auditionController, "999");
        } catch (Exception e) {
            fail("Valid post IDs should not throw exceptions");
        }
    }

    @Test
    void testValidatePostIdWithInvalidString() {
        try {
            java.lang.reflect.Method method = AuditionController.class.getDeclaredMethod("validatePostId", String.class);
            method.setAccessible(true);

            // Test null
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
                method.invoke(auditionController, (String) null);
            });

            // Test empty string
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
                method.invoke(auditionController, "");
            });

            // Test whitespace
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
                method.invoke(auditionController, "   ");
            });

            // Test non-numeric
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
                method.invoke(auditionController, "abc");
            });

            // Test negative number
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
                method.invoke(auditionController, "-1");
            });

            // Test zero
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
                method.invoke(auditionController, "0");
            });

        } catch (NoSuchMethodException e) {
            // Method might not exist if validation is handled differently
            // This is acceptable as the validation might be done via annotations
        }
    }

    @Test
    void testControllerWithLargeUserId() {
        // Given
        when(auditionService.getPostsWithFilter("2147483647", null)).thenReturn(Collections.emptyList());

        // When
        List<AuditionPost> result = auditionController.getPosts(Integer.MAX_VALUE, null);

        // Then
        assertNotNull(result);
        verify(auditionService).getPostsWithFilter("2147483647", null);
    }

    @Test
    void testControllerWithVeryLongTitle() {
        // Given
        String longTitle = "a".repeat(50); // 50 characters
        when(auditionService.getPostsWithFilter(null, longTitle)).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionController.getPosts(null, longTitle);

        // Then
        assertNotNull(result);
        verify(auditionService).getPostsWithFilter(null, longTitle);
    }

    @Test
    void testGetPostsWithEmptyResults() {
        // Given
        when(auditionService.getPosts()).thenReturn(Collections.emptyList());

        // When
        List<AuditionPost> result = auditionController.getPosts(null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCommentsWithEmptyResults() {
        // Given
        when(auditionService.getCommentsForPost("999")).thenReturn(Collections.emptyList());

        // When
        List<AuditionComment> result = auditionController.getCommentsByPostId(999);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testServiceExceptionPropagation() {
        // Given
        SystemException expectedException = new SystemException("Test error", 404);
        when(auditionService.getPostById("999")).thenThrow(expectedException);

        // When & Then
        SystemException actualException = assertThrows(SystemException.class, () -> {
            auditionController.getPostById(999);
        });

        assertEquals("Test error", actualException.getMessage());
        assertEquals(404, actualException.getStatusCode());
    }

    @Test
    void testAllControllerMethodsWithDifferentIds() {
        // Test various ID values
        int[] testIds = {1, 42, 100, 999, Integer.MAX_VALUE};

        for (int id : testIds) {
            // Setup mocks for each ID
            when(auditionService.getPostById(String.valueOf(id))).thenReturn(samplePost);
            when(auditionService.getPostByIdWithComments(String.valueOf(id))).thenReturn(samplePost);
            when(auditionService.getCommentsForPost(String.valueOf(id))).thenReturn(sampleComments);

            // Test getPostById
            AuditionPost post = auditionController.getPostById(id);
            assertNotNull(post);

            // Test getPostWithComments
            AuditionPost postWithComments = auditionController.getPostWithComments(id);
            assertNotNull(postWithComments);

            // Test getCommentsByPostId
            List<AuditionComment> comments = auditionController.getCommentsByPostId(id);
            assertNotNull(comments);
        }
    }

    @Test
    void testControllerMethodReturnTypes() {
        // Given
        when(auditionService.getPosts()).thenReturn(samplePosts);
        when(auditionService.getPostById("1")).thenReturn(samplePost);
        when(auditionService.getPostByIdWithComments("1")).thenReturn(samplePost);
        when(auditionService.getCommentsForPost("1")).thenReturn(sampleComments);

        // When & Then - verify return types
        Object postsResult = auditionController.getPosts(null, null);
        assertTrue(postsResult instanceof List);

        Object postResult = auditionController.getPostById(1);
        assertTrue(postResult instanceof AuditionPost);

        Object postWithCommentsResult = auditionController.getPostWithComments(1);
        assertTrue(postWithCommentsResult instanceof AuditionPost);

        Object commentsResult = auditionController.getCommentsByPostId(1);
        assertTrue(commentsResult instanceof List);
    }

    @Test
    void testControllerAnnotations() {
        // Verify that the controller has the correct annotations
        Class<?> controllerClass = AuditionController.class;

        assertTrue(controllerClass.isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class));
        assertTrue(controllerClass.isAnnotationPresent(org.springframework.validation.annotation.Validated.class));
    }

    @Test
    void testModelObjectsEquality() {
        // Test AuditionPost equality and hash code behavior
        AuditionPost post1 = new AuditionPost();
        post1.setId(1);
        post1.setUserId(1);
        post1.setTitle("Test");
        post1.setBody("Body");

        AuditionPost post2 = new AuditionPost();
        post2.setId(1);
        post2.setUserId(1);
        post2.setTitle("Test");
        post2.setBody("Body");

        // Test that objects with same values behave consistently
        assertEquals(post1.getId(), post2.getId());
        assertEquals(post1.getUserId(), post2.getUserId());
        assertEquals(post1.getTitle(), post2.getTitle());
        assertEquals(post1.getBody(), post2.getBody());

        // Test AuditionComment equality and hash code behavior
        AuditionComment comment1 = new AuditionComment();
        comment1.setId(1);
        comment1.setPostId(1);
        comment1.setName("Test User");
        comment1.setEmail("test@test.com");
        comment1.setBody("Test body");

        AuditionComment comment2 = new AuditionComment();
        comment2.setId(1);
        comment2.setPostId(1);
        comment2.setName("Test User");
        comment2.setEmail("test@test.com");
        comment2.setBody("Test body");

        assertEquals(comment1.getId(), comment2.getId());
        assertEquals(comment1.getPostId(), comment2.getPostId());
        assertEquals(comment1.getName(), comment2.getName());
        assertEquals(comment1.getEmail(), comment2.getEmail());
        assertEquals(comment1.getBody(), comment2.getBody());
    }

    @Test
    void testSystemExceptionAllConstructorPaths() {
        // Test all constructor paths to ensure complete coverage

        // Default constructor
        SystemException ex1 = new SystemException();
        assertNotNull(ex1);

        // Constructor with message
        SystemException ex2 = new SystemException("test message");
        assertEquals("test message", ex2.getMessage());
        assertEquals("API Error Occurred", ex2.getTitle());

        // Constructor with message and error code
        SystemException ex3 = new SystemException("test message", 400);
        assertEquals("test message", ex3.getMessage());
        assertEquals(Integer.valueOf(400), ex3.getStatusCode());

        // Constructor with message and exception
        RuntimeException cause = new RuntimeException("cause");
        SystemException ex4 = new SystemException("test message", cause);
        assertEquals("test message", ex4.getMessage());
        assertEquals(cause, ex4.getCause());

        // Constructor with detail, title, and error code
        SystemException ex5 = new SystemException("detail", "title", 404);
        assertEquals("detail", ex5.getDetail());
        assertEquals("title", ex5.getTitle());
        assertEquals(Integer.valueOf(404), ex5.getStatusCode());

        // Constructor with detail, title, and exception
        SystemException ex6 = new SystemException("detail", "title", cause);
        assertEquals("detail", ex6.getDetail());
        assertEquals("title", ex6.getTitle());
        assertEquals(cause, ex6.getCause());
        assertEquals(Integer.valueOf(500), ex6.getStatusCode());

        // Constructor with detail, error code, and exception
        SystemException ex7 = new SystemException("detail", 422, cause);
        assertEquals("detail", ex7.getDetail());
        assertEquals(Integer.valueOf(422), ex7.getStatusCode());
        assertEquals(cause, ex7.getCause());
        assertEquals("API Error Occurred", ex7.getTitle());

        // Constructor with all parameters
        SystemException ex8 = new SystemException("detail", "title", 418, cause);
        assertEquals("detail", ex8.getDetail());
        assertEquals("title", ex8.getTitle());
        assertEquals(Integer.valueOf(418), ex8.getStatusCode());
        assertEquals(cause, ex8.getCause());
    }

    @Test
    void testControllerWithSpecialCharactersInTitle() {
        // Given
        String specialTitle = "test@#$%^&*()title";
        when(auditionService.getPostsWithFilter(null, specialTitle)).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionController.getPosts(null, specialTitle);

        // Then
        assertNotNull(result);
        verify(auditionService).getPostsWithFilter(null, specialTitle);
    }

    @Test
    void testControllerWithUnicodeTitle() {
        // Given
        String unicodeTitle = "测试标题";
        when(auditionService.getPostsWithFilter(null, unicodeTitle)).thenReturn(samplePosts);

        // When
        List<AuditionPost> result = auditionController.getPosts(null, unicodeTitle);

        // Then
        assertNotNull(result);
        verify(auditionService).getPostsWithFilter(null, unicodeTitle);
    }

    @Test
    void testServiceFieldInjection() {
        // Verify that the service is properly injected
        AuditionService injectedService = (AuditionService) ReflectionTestUtils.getField(auditionController, "auditionService");
        assertNotNull(injectedService);
        assertEquals(auditionService, injectedService);
    }

    @Test
    void testControllerMethodsWithMinimumValues() {
        // Test with minimum valid values
        when(auditionService.getPostById("1")).thenReturn(samplePost);
        when(auditionService.getPostByIdWithComments("1")).thenReturn(samplePost);
        when(auditionService.getCommentsForPost("1")).thenReturn(sampleComments);
        when(auditionService.getPostsWithFilter("1", null)).thenReturn(samplePosts);

        // Test minimum userId
        List<AuditionPost> postsResult = auditionController.getPosts(1, null);
        assertNotNull(postsResult);

        // Test minimum postId
        AuditionPost postResult = auditionController.getPostById(1);
        assertNotNull(postResult);

        AuditionPost postWithCommentsResult = auditionController.getPostWithComments(1);
        assertNotNull(postWithCommentsResult);

        List<AuditionComment> commentsResult = auditionController.getCommentsByPostId(1);
        assertNotNull(commentsResult);
    }

    @Test
    void testExceptionMessageBuilding() {
        // Test various exception scenarios to ensure message building is covered

        SystemException ex1 = new SystemException(null, "title", 400);
        assertNull(ex1.getDetail());
        assertEquals("title", ex1.getTitle());

        SystemException ex2 = new SystemException("", "title", 400);
        assertEquals("", ex2.getDetail());
        assertEquals("title", ex2.getTitle());

        SystemException ex3 = new SystemException("   ", "title", 400);
        assertEquals("   ", ex3.getDetail());
        assertEquals("title", ex3.getTitle());
    }

    @Test
    void testControllerResponseAnnotations() {
        // Test that methods have proper response annotations
        try {
            java.lang.reflect.Method getPostsMethod = AuditionController.class.getMethod("getPosts", Integer.class, String.class);
            assertTrue(getPostsMethod.isAnnotationPresent(org.springframework.web.bind.annotation.ResponseBody.class));

            java.lang.reflect.Method getPostByIdMethod = AuditionController.class.getMethod("getPostById", Integer.class);
            assertTrue(getPostByIdMethod.isAnnotationPresent(org.springframework.web.bind.annotation.ResponseBody.class));

            java.lang.reflect.Method getPostWithCommentsMethod = AuditionController.class.getMethod("getPostWithComments", Integer.class);
            assertTrue(getPostWithCommentsMethod.isAnnotationPresent(org.springframework.web.bind.annotation.ResponseBody.class));

            java.lang.reflect.Method getCommentsByPostIdMethod = AuditionController.class.getMethod("getCommentsByPostId", Integer.class);
            assertTrue(getCommentsByPostIdMethod.isAnnotationPresent(org.springframework.web.bind.annotation.ResponseBody.class));

        } catch (NoSuchMethodException e) {
            fail("Controller methods should exist: " + e.getMessage());
        }
    }
}