package oth.ics.wtp.relaybackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.dtos.*;

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
        userController.register(basic("poster1", "1234"));
        userController.register(basic("poster2", "2345"));
        user1Req = mockRequest("poster1", "1234");
        user2Req = mockRequest("poster2", "2345");
    }

    @Test
    public void testCreateList() {
        long id1 = controller.createPost(new CreatePostDto("post1"), user1Req).id();
        controller.createPost(new CreatePostDto("post2"), user1Req);
        controller.createPost(new CreatePostDto("post3"), user2Req);

        List<PostDto> posts = controller.getUserPosts("poster1", user1Req);
        assertEquals(2, posts.size());
        assertEquals("post2", posts.get(0).content());
        assertEquals("post1", posts.get(1).content());
    }

    @Test
    public void testTimeline() {
        userController.followUser("poster2", user1Req);

        controller.createPost(new CreatePostDto("own post"), user1Req);
        long id2 = controller.createPost(new CreatePostDto("followed post"), user2Req).id();

        List<PostDto> timeline = controller.getTimeline(user1Req);
        assertEquals(1, timeline.size());
        assertEquals(id2, timeline.get(0).id());
    }

    @Test
    public void testLikeUnlike() {
        long postId = controller.createPost(new CreatePostDto("nice post"), user1Req).id();

        controller.likePost(postId, user2Req);
        PostDto liked = controller.getUserPosts("poster1", user2Req).get(0);
        assertEquals(1, liked.likeCount());
        assertTrue(liked.isLikedByCurrentUser());

        controller.unlikePost(postId, user2Req);
        PostDto unliked = controller.getUserPosts("poster1", user2Req).get(0);
        assertEquals(0, unliked.likeCount());
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
}