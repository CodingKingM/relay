package oth.ics.wtp.relaybackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.dtos.*;
import java.util.Optional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PostControllerTest extends RelayControllerTestBase {
    @Autowired private PostController controller;
    @Autowired private UserController userController;

    private HttpServletRequest user1Req;
    private HttpServletRequest user2Req;

    @BeforeEach
    @Override
    public void beforeEach() {
        super.beforeEach();
        user1Req = mockRequest(USER1_USERNAME, USER1_PASSWORD);
        user2Req = mockRequest(USER2_USERNAME, USER2_PASSWORD);
    }

    @Test
    public void testCreateList() {
        long id1 = controller.createPost(new CreatePostDto("post1"), user1Req).id();
        controller.createPost(new CreatePostDto("post2"), user1Req);
        controller.createPost(new CreatePostDto("post3"), user2Req);

        List<PostDto> posts = controller.getUserPosts(USER1_USERNAME, user1Req);
        assertEquals(2, posts.size());
        assertEquals("post2", posts.get(0).content());
        assertEquals("post1", posts.get(1).content());
    }

    @Test
    public void testTimeline() {
        userController.followUser(USER2_USERNAME, user1Req);

        controller.createPost(new CreatePostDto("own post"), user1Req);
        long id2 = controller.createPost(new CreatePostDto("followed post"), user2Req).id();

        List<PostDto> timeline = controller.getTimeline(user1Req);
        assertEquals(1, timeline.size());
        assertEquals(id2, timeline.get(0).id());
    }

    @Test
    public void testPostLikeAndUnlike() throws InterruptedException {
        long postId = controller.createPost(new CreatePostDto("A great post!"), user1Req).id();
        
        controller.likePost(postId, user2Req);

        Thread.sleep(500); // 0.5 second delay

        List<PostDto> postsAfterLike = controller.getUserPosts(USER1_USERNAME, user2Req);
        Optional<PostDto> likedPostOptional = findPostInList(postsAfterLike, postId);

        assertTrue(likedPostOptional.isPresent(), "The post should be found after being liked.");
        PostDto likedPost = likedPostOptional.get();

        assertEquals(1, likedPost.likeCount(), "The like count should be 1 after liking.");
        assertTrue(likedPost.isLikedByCurrentUser(), "The post should be marked as liked by the current user.");

        // Debug: Check if like exists before unliking
        System.out.println("Before unlike - Like count: " + likedPost.likeCount() + ", Is liked: " + likedPost.isLikedByCurrentUser());

        controller.unlikePost(postId, user2Req);

        Thread.sleep(500); // Allow DB state to update

        List<PostDto> postsAfterUnlike = controller.getUserPosts(USER1_USERNAME, user2Req);
        Optional<PostDto> unlikedPostOptional = findPostInList(postsAfterUnlike, postId);

        assertTrue(unlikedPostOptional.isPresent(), "The post should still be found after being unliked.");
        PostDto unlikedPost = unlikedPostOptional.get();

        // Debug: Check the actual values
        System.out.println("After unlike - Like count: " + unlikedPost.likeCount() + ", Is liked: " + unlikedPost.isLikedByCurrentUser());

        assertEquals(0, unlikedPost.likeCount(), "The like count should be 0 after unliking.");
        assertFalse(unlikedPost.isLikedByCurrentUser(), "The post should no longer be marked as liked.");
    }

    private Optional<PostDto> findPostInList(List<PostDto> postList, long postId) {
        return postList.stream()
                .filter(post -> post.id() == postId)
                .findFirst();
    }

    @Test
    public void testCannotLikeOwn() {
        long postId = controller.createPost(new CreatePostDto("my post"), user1Req).id();
        assertThrows(ResponseStatusException.class, () -> controller.likePost(postId, user1Req));
    }

    @Test
    public void testEmptyContent() {
        assertThrows(ResponseStatusException.class,
                () -> controller.createPost(new CreatePostDto(""), user1Req));
    }

    @Test
    public void testNullContent() {
        assertThrows(ResponseStatusException.class,
                () -> controller.createPost(new CreatePostDto(null), user1Req));
    }

    @Test
    public void testContentTooLong() {
        String longContent = "a".repeat(281); // 281 characters, exceeding 280 limit
        assertThrows(ResponseStatusException.class,
                () -> controller.createPost(new CreatePostDto(longContent), user1Req));
    }

    @Test
    public void testContentExactly280Characters() {
        String exactContent = "a".repeat(280); // Exactly 280 characters
        assertDoesNotThrow(() -> controller.createPost(new CreatePostDto(exactContent), user1Req));
    }

    @Test
    public void testLikeNonExistentPost() {
        assertThrows(ResponseStatusException.class,
                () -> controller.likePost(99999L, user2Req));
    }

    @Test
    public void testUnlikeNonExistentPost() {
        assertThrows(ResponseStatusException.class,
                () -> controller.unlikePost(99999L, user2Req));
    }

    @Test
    public void testUnlikePostNotLiked() {
        long postId = controller.createPost(new CreatePostDto("test post"), user1Req).id();
        // user2 hasn't liked the post, so unliking should not throw, just remain at 0 likes
        assertDoesNotThrow(() -> controller.unlikePost(postId, user2Req));
        List<PostDto> posts = controller.getUserPosts(USER1_USERNAME, user2Req);
        Optional<PostDto> post = findPostInList(posts, postId);
        assertTrue(post.isPresent());
        assertEquals(0, post.get().likeCount());
    }

    @Test
    public void testGetUserPostsForNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> controller.getUserPosts("nonexistentuser", user1Req));
    }

    @Test
    public void testGetUserPostsWithoutAuthentication() {
        // Create a request without proper authentication
        HttpServletRequest unauthenticatedReq = new MockHttpServletRequest();
        // Should not throw; should return posts (possibly not personalized)
        assertDoesNotThrow(() -> controller.getUserPosts(USER1_USERNAME, unauthenticatedReq));
    }

    @Test
    public void testCreatePostWithoutAuthentication() {
        // Create a request without proper authentication
        HttpServletRequest unauthenticatedReq = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class,
                () -> controller.createPost(new CreatePostDto("test"), unauthenticatedReq));
    }

    @Test
    public void testGetTimelineWithoutAuthentication() {
        // Create a request without proper authentication
        HttpServletRequest unauthenticatedReq = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class,
                () -> controller.getTimeline(unauthenticatedReq));
    }

    @Test
    public void testLikePostWithoutAuthentication() {
        long postId = controller.createPost(new CreatePostDto("test post"), user1Req).id();
        // Create a request without proper authentication
        HttpServletRequest unauthenticatedReq = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class,
                () -> controller.likePost(postId, unauthenticatedReq));
    }

    @Test
    public void testUnlikePostWithoutAuthentication() {
        long postId = controller.createPost(new CreatePostDto("test post"), user1Req).id();
        // Create a request without proper authentication
        HttpServletRequest unauthenticatedReq = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class,
                () -> controller.unlikePost(postId, unauthenticatedReq));
    }
}