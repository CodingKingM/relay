package oth.ics.wtp.relaybackend.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import oth.ics.wtp.relaybackend.dtos.CreatePostDto;
import oth.ics.wtp.relaybackend.dtos.PostDto;
import oth.ics.wtp.relaybackend.entities.Comment;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.services.AuthService;
import oth.ics.wtp.relaybackend.services.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@SecurityRequirement(name = "basicAuth")
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    public PostController(PostService postService, AuthService authService) {
        this.postService = postService;
        this.authService = authService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto createPost(
            @RequestBody CreatePostDto createPostDto,
            HttpServletRequest request) {
        User currentUser = authService.getAuthenticatedUser(request);
        return postService.createPost(createPostDto, currentUser.getUsername());
    }

    @GetMapping(value = "/timeline", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PostDto> getTimeline(HttpServletRequest request) {
        User currentUser = authService.getAuthenticatedUser(request);
        return postService.getTimelinePosts(currentUser.getUsername());
    }

    @GetMapping(value = "/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PostDto> getUserPosts(
            @PathVariable String username,
            HttpServletRequest request) {
        String currentUsername = authService.getAuthenticatedUsername(request).orElse(null);
        return postService.getUserPosts(username, currentUsername);
    }

    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likePost(
            @PathVariable Long postId,
            HttpServletRequest request) {
        User currentUser = authService.getAuthenticatedUser(request);
        postService.likePost(postId, currentUser.getUsername());
    }

    @DeleteMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikePost(
            @PathVariable Long postId,
            HttpServletRequest request) {
        User currentUser = authService.getAuthenticatedUser(request);
        postService.unlikePost(postId, currentUser.getUsername());
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId, HttpServletRequest request) {
        System.out.println("DEBUG: Entered PostController.deletePost for postId=" + postId);
        User currentUser = authService.getAuthenticatedUser(request);
        postService.deletePost(postId, currentUser.getUsername());
    }

    @GetMapping("/{postId}/comments")
    public List<Comment> getComments(@PathVariable Long postId) {
        return postService.getCommentsForPost(postId);
    }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment addComment(@PathVariable Long postId, @RequestBody String content, HttpServletRequest request) {
        User currentUser = authService.getAuthenticatedUser(request);
        return postService.addCommentToPost(postId, currentUser.getUsername(), content);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        User currentUser = authService.getAuthenticatedUser(request);
        postService.deleteComment(commentId, currentUser.getUsername());
    }
}