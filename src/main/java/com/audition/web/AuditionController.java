package com.audition.web;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import com.audition.service.AuditionService;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditionController {

    @Autowired
    AuditionService auditionService;

    @RequestMapping(value = "/posts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<AuditionPost> getPosts(
            @RequestParam(required = false) final String userId,
            @RequestParam(required = false) final String title) {

        // Add logic that filters response data based on the query param
        if (StringUtils.isNotBlank(userId) || StringUtils.isNotBlank(title)) {
            return auditionService.getPostsWithFilter(userId, title);
        }

        return auditionService.getPosts();
    }

    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AuditionPost getPostById(@PathVariable("id") final String postId) {
        // Add input validation
        validatePostId(postId);

        return auditionService.getPostById(postId);
    }

    @GetMapping(value = "/posts/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AuditionPost getPostWithComments(@PathVariable("id") final String postId) {
        // Add input validation
        validatePostId(postId);

        return auditionService.getPostByIdWithComments(postId);
    }

    @GetMapping(value = "/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<AuditionComment> getCommentsByPostId(
            @RequestParam("postId") final String postId) {
        // Add input validation
        validatePostId(postId);

        return auditionService.getCommentsForPost(postId);
    }

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