package oth.ics.wtp.relaybackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.dtos.*;
import oth.ics.wtp.relaybackend.repositories.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest extends RelayControllerTestBase {
    @Autowired private UserController controller;
    @Autowired private PostController postController;
    @Autowired private UserRepository userRepository;

    @Test
    public void testCreateLogin() {
        controller.register(basic("user42", "9876"));
        UserDto loggedIn = controller.login(mockRequest("user42", "9876"));
        assertEquals("user42", loggedIn.username());

        controller.logout(mockRequest("user42", "9876"));
        assertThrows(ResponseStatusException.class,
                () -> postController.getTimeline(mockRequest("user42", "9876")));
    }

    @Test
    public void testSearchUsers() {
        controller.register(basic("alice", "1234"));
        controller.register(basic("alex", "2345"));
        controller.register(basic("bob", "3456"));

        List<UserSearchDto> results = controller.searchUsers("al", user1());
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> u.username().equals("alice")));
    }

    @Test
    public void testFollowUnfollow() {
        controller.register(basic("follower1", "pass"));
        controller.register(basic("followed1", "pass"));

        HttpServletRequest req = mockRequest("follower1", "pass");
        controller.followUser("followed1", req);

        List<UserSearchDto> search = controller.searchUsers("followed1", req);
        assertTrue(search.get(0).isFollowing());

        controller.unfollowUser("followed1", req);
        search = controller.searchUsers("followed1", req);
        assertFalse(search.get(0).isFollowing());
    }

    @Test
    public void testDeleteUserWithPosts() {
        controller.register(basic("user99", "pass"));
        postController.createPost(new CreatePostDto("post1"), mockRequest("user99", "pass"));

        // In real app this might need cascade handling
        assertDoesNotThrow(() -> userRepository.deleteById("user99"));
    }
}