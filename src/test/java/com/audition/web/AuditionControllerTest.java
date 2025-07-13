package com.audition.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import com.audition.service.AuditionService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditionController.class)
@TestPropertySource(properties = {
        "spring.sleuth.enabled=false",
        "management.tracing.enabled=false"
})
class AuditionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditionService auditionService;

    private AuditionPost samplePost;
    private List<AuditionPost> samplePosts;
    private List<AuditionComment> sampleComments;

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
    void testGetPosts() throws Exception {
        // Given
        when(auditionService.getPosts()).thenReturn(samplePosts);

        // When & Then
        mockMvc.perform(get("/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Sample Post"))
                .andExpect(jsonPath("$[1].title").value("Another Post"));
    }

    @Test
    void testGetPostsWithFilter() throws Exception {
        // Given
        when(auditionService.getPostsWithFilter("1", null)).thenReturn(Arrays.asList(samplePost));

        // When & Then
        mockMvc.perform(get("/posts")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    void testGetPostById() throws Exception {
        // Given
        when(auditionService.getPostById("1")).thenReturn(samplePost);

        // When & Then
        mockMvc.perform(get("/posts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Sample Post"));
    }

    @Test
    void testGetPostByIdWithInvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/posts/invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPostByIdWithEmptyId() throws Exception {
        // When & Then
        mockMvc.perform(get("/posts/ ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPostByIdWithNegativeId() throws Exception {
        // When & Then
        mockMvc.perform(get("/posts/-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPostByIdNotFound() throws Exception {
        // Given
        when(auditionService.getPostById("999")).thenThrow(
                new SystemException("Cannot find a Post with id 999", "Resource Not Found", 404));

        // When & Then
        mockMvc.perform(get("/posts/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPostWithComments() throws Exception {
        // Given
        samplePost.setComments(sampleComments);
        when(auditionService.getPostByIdWithComments("1")).thenReturn(samplePost);

        // When & Then
        mockMvc.perform(get("/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].body").value("Great post!"));
    }

    @Test
    void testGetCommentsByPostId() throws Exception {
        // Given
        when(auditionService.getCommentsForPost("1")).thenReturn(sampleComments);

        // When & Then
        mockMvc.perform(get("/comments")
                        .param("postId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].body").value("Great post!"));
    }

    @Test
    void testGetCommentsByPostIdWithInvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/comments")
                        .param("postId", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}