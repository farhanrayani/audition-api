package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditionService {

    @Autowired
    private AuditionIntegrationClient auditionIntegrationClient;

    public List<AuditionPost> getPosts() {
        return auditionIntegrationClient.getPosts();
    }

    public List<AuditionPost> getPostsWithFilter(final String userIdFilter, final String titleFilter) {
        List<AuditionPost> posts = auditionIntegrationClient.getPosts();

        return posts.stream()
                .filter(post -> {
                    boolean matches = true;

                    if (StringUtils.isNotBlank(userIdFilter)) {
                        try {
                            int userId = Integer.parseInt(userIdFilter);
                            matches = matches && post.getUserId() == userId;
                        } catch (NumberFormatException e) {
                            // If userIdFilter is not a valid integer, no posts will match
                            return false;
                        }
                    }

                    if (StringUtils.isNotBlank(titleFilter)) {
                        matches = matches && post.getTitle() != null &&
                                post.getTitle().toLowerCase().contains(titleFilter.toLowerCase());
                    }

                    return matches;
                })
                .collect(Collectors.toList());
    }

    public AuditionPost getPostById(final String postId) {
        return auditionIntegrationClient.getPostById(postId);
    }

    public AuditionPost getPostByIdWithComments(final String postId) {
        return auditionIntegrationClient.getPostByIdWithComments(postId);
    }

    public List<AuditionComment> getCommentsForPost(final String postId) {
        return auditionIntegrationClient.getCommentsByPostId(postId);
    }
}