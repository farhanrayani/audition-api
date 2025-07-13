package com.audition.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AuditionModelTest {

    @Test
    void testAuditionPostGettersAndSetters() {
        // Given
        AuditionPost post = new AuditionPost();
        int userId = 1;
        int id = 123;
        String title = "Test Post Title";
        String body = "Test post body content";

        AuditionComment comment1 = new AuditionComment();
        comment1.setId(1);
        comment1.setBody("First comment");

        AuditionComment comment2 = new AuditionComment();
        comment2.setId(2);
        comment2.setBody("Second comment");

        List<AuditionComment> comments = Arrays.asList(comment1, comment2);

        // When
        post.setUserId(userId);
        post.setId(id);
        post.setTitle(title);
        post.setBody(body);
        post.setComments(comments);

        // Then
        assertEquals(userId, post.getUserId());
        assertEquals(id, post.getId());
        assertEquals(title, post.getTitle());
        assertEquals(body, post.getBody());
        assertEquals(comments, post.getComments());
        assertEquals(2, post.getComments().size());
        assertEquals("First comment", post.getComments().get(0).getBody());
        assertEquals("Second comment", post.getComments().get(1).getBody());
    }

    @Test
    void testAuditionCommentGettersAndSetters() {
        // Given
        AuditionComment comment = new AuditionComment();
        int postId = 456;
        int id = 789;
        String name = "John Doe";
        String email = "john.doe@example.com";
        String body = "This is a test comment";

        // When
        comment.setPostId(postId);
        comment.setId(id);
        comment.setName(name);
        comment.setEmail(email);
        comment.setBody(body);

        // Then
        assertEquals(postId, comment.getPostId());
        assertEquals(id, comment.getId());
        assertEquals(name, comment.getName());
        assertEquals(email, comment.getEmail());
        assertEquals(body, comment.getBody());
    }

    @Test
    void testAuditionPostWithNullValues() {
        // Given
        AuditionPost post = new AuditionPost();

        // When - setting null values
        post.setTitle(null);
        post.setBody(null);
        post.setComments(null);

        // Then
        assertNull(post.getTitle());
        assertNull(post.getBody());
        assertNull(post.getComments());
        assertEquals(0, post.getUserId()); // primitive int defaults to 0
        assertEquals(0, post.getId());     // primitive int defaults to 0
    }

    @Test
    void testAuditionCommentWithNullValues() {
        // Given
        AuditionComment comment = new AuditionComment();

        // When - setting null values
        comment.setName(null);
        comment.setEmail(null);
        comment.setBody(null);

        // Then
        assertNull(comment.getName());
        assertNull(comment.getEmail());
        assertNull(comment.getBody());
        assertEquals(0, comment.getPostId()); // primitive int defaults to 0
        assertEquals(0, comment.getId());     // primitive int defaults to 0
    }

    @Test
    void testAuditionPostWithEmptyCommentsList() {
        // Given
        AuditionPost post = new AuditionPost();
        List<AuditionComment> emptyComments = Arrays.asList();

        // When
        post.setComments(emptyComments);

        // Then
        assertNotNull(post.getComments());
        assertTrue(post.getComments().isEmpty());
        assertEquals(0, post.getComments().size());
    }

    @Test
    void testAuditionCommentWithEmptyStrings() {
        // Given
        AuditionComment comment = new AuditionComment();

        // When
        comment.setName("");
        comment.setEmail("");
        comment.setBody("");

        // Then
        assertEquals("", comment.getName());
        assertEquals("", comment.getEmail());
        assertEquals("", comment.getBody());
    }

    @Test
    void testAuditionPostWithEmptyStrings() {
        // Given
        AuditionPost post = new AuditionPost();

        // When
        post.setTitle("");
        post.setBody("");

        // Then
        assertEquals("", post.getTitle());
        assertEquals("", post.getBody());
    }

    @Test
    void testObjectCreationWithDefaultValues() {
        // Given & When
        AuditionPost post = new AuditionPost();
        AuditionComment comment = new AuditionComment();

        // Then - verify default values
        assertEquals(0, post.getUserId());
        assertEquals(0, post.getId());
        assertNull(post.getTitle());
        assertNull(post.getBody());
        assertNull(post.getComments());

        assertEquals(0, comment.getPostId());
        assertEquals(0, comment.getId());
        assertNull(comment.getName());
        assertNull(comment.getEmail());
        assertNull(comment.getBody());
    }
}