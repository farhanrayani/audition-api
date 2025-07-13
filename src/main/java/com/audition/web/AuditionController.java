package com.audition.web;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import com.audition.service.AuditionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Tag(name = "Posts", description = "Posts management API")
public class AuditionController {

    @Autowired
    AuditionService auditionService;

    @Operation(
            summary = "Get all posts",
            description = "Retrieve all posts with optional filtering by userId and title"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved posts"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestMapping(value = "/posts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<AuditionPost> getPosts(
            @Parameter(description = "Filter by user ID (must be positive)")
            @RequestParam(required = false)
            @Min(value = 1, message = "User ID must be positive")
            @Max(value = Integer.MAX_VALUE, message = "User ID too large")
            final Integer userId,
            @Parameter(description = "Filter by title (case-insensitive, 1-100 characters)")
            @RequestParam(required = false)
            @Size(min = 1, max = 100, message = "Title filter must be between 1 and 100 characters")
            final String title) {

        // Add logic that filters response data based on the query param
        if (userId != null || StringUtils.isNotBlank(title)) {
            return auditionService.getPostsWithFilter(
                    userId != null ? userId.toString() : null,
                    title);
        }

        return auditionService.getPosts();
    }

    @Operation(
            summary = "Get post by ID",
            description = "Retrieve a specific post by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved post"),
            @ApiResponse(responseCode = "400", description = "Invalid post ID"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AuditionPost getPostById(
            @Parameter(description = "Post ID (must be positive)", required = true)
            @PathVariable("id")
            @Min(value = 1, message = "Post ID must be positive")
            @Max(value = Integer.MAX_VALUE, message = "Post ID too large")
            final Integer postId) {

        return auditionService.getPostById(postId.toString());
    }

    @Operation(
            summary = "Get post with comments",
            description = "Retrieve a specific post along with all its comments"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved post with comments"),
            @ApiResponse(responseCode = "400", description = "Invalid post ID"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/posts/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AuditionPost getPostWithComments(
            @Parameter(description = "Post ID (must be positive)", required = true)
            @PathVariable("id")
            @Min(value = 1, message = "Post ID must be positive")
            @Max(value = Integer.MAX_VALUE, message = "Post ID too large")
            final Integer postId) {

        return auditionService.getPostByIdWithComments(postId.toString());
    }

    @Operation(
            summary = "Get comments by post ID",
            description = "Retrieve all comments for a specific post"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved comments"),
            @ApiResponse(responseCode = "400", description = "Invalid post ID"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<AuditionComment> getCommentsByPostId(
            @Parameter(description = "Post ID (must be positive)", required = true)
            @RequestParam("postId")
            @Min(value = 1, message = "Post ID must be positive")
            @Max(value = Integer.MAX_VALUE, message = "Post ID too large")
            final Integer postId) {

        return auditionService.getCommentsForPost(postId.toString());
    }

    /**
     * Legacy validation method - kept for backward compatibility
     * The new validation uses Bean Validation annotations
     */
    private void validatePostId(final String postId) {
        if (StringUtils.isBlank(postId)) {
            throw new SystemException("Post ID cannot be null or empty", "Invalid Input", 400);
        }

        try {
            int id = Integer.parseInt(postId);
            if (id <= 0) {
                throw new SystemException("Post ID must be a positive integer", "Invalid Input", 400);
            }
        } catch (NumberFormatException e) {
            throw new SystemException("Post ID must be a valid integer", "Invalid Input", 400);
        }
    }
}