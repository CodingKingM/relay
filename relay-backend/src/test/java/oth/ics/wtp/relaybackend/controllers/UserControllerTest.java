package oth.ics.wtp.relaybackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.dtos.*;
import oth.ics.wtp.relaybackend.repositories.UserRepository;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest extends RelayControllerTestBase {

    @Autowired private UserController controller;
    @Autowired private PostController postController;
    @Autowired private UserRepository userRepository;

    @Test
    public void testCreateLogin() {
        // Use a unique username to avoid conflicts with base setup
        String username = USER1_USERNAME + "_login";
        String password = USER1_PASSWORD + "_login";

        // Register user
        controller.register(basic(username, password));

        // Login user - this should work
        UserDto loggedIn = controller.login(mockRequest(username, password));
        assertEquals(username, loggedIn.username());

        // Logout user
        controller.logout(mockRequest(username, password));

        // Simulate a new request after logout (no session)
        HttpServletRequest afterLogoutReq = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class,
                () -> postController.getTimeline(afterLogoutReq));
    }

    @Test
    public void testSearchUsers() {
        // Use the pre-created users from base class or create new ones
        controller.register(basic(USER1_USERNAME + "_alice", USER1_PASSWORD));
        controller.register(basic(USER1_USERNAME + "_alex", USER1_PASSWORD));
        controller.register(basic(USER1_USERNAME + "_bob", USER1_PASSWORD));

        // Login user1 first to establish proper session
        controller.login(user1());

        List<UserSearchDto> results = controller.searchUsers(USER1_USERNAME.substring(0, 5), user1());
        assertTrue(results.size() >= 1);
        assertTrue(results.stream().anyMatch(u -> u.username().contains(USER1_USERNAME.substring(0, 5))));
    }

    @Test
    public void testGetUserByUsername() {
        String username1 = USER1_USERNAME;
        UserDto User1 = controller.getUser(username1);
        assertEquals(username1, User1.username());

        String username2 = "nonExistentUser" + USER1_USERNAME;
        assertThrows(ResponseStatusException.class, () -> controller.getUser(username2));
    }

    @Test
    public void testFollowUser() {
        // Register and login users
        controller.register(basic("follower", "pass1"));
        controller.register(basic("followed", "pass2"));
        
        HttpServletRequest followerReq = mockRequest("follower", "pass1");
        controller.login(followerReq);

        // Follow user
        assertDoesNotThrow(() -> controller.followUser("followed", followerReq));
    }

    @Test
    public void testFollowNonExistentUser() {
        controller.register(basic("follower", "pass1"));
        HttpServletRequest followerReq = mockRequest("follower", "pass1");
        controller.login(followerReq);

        assertThrows(ResponseStatusException.class,
                () -> controller.followUser("nonexistent", followerReq));
    }

    @Test
    public void testFollowSelf() {
        controller.register(basic("user", "pass1"));
        HttpServletRequest userReq = mockRequest("user", "pass1");
        controller.login(userReq);

        assertThrows(ResponseStatusException.class,
                () -> controller.followUser("user", userReq));
    }

    @Test
    public void testUnfollowUser() {
        // Register and login users
        controller.register(basic("follower", "pass1"));
        controller.register(basic("followed", "pass2"));
        
        HttpServletRequest followerReq = mockRequest("follower", "pass1");
        controller.login(followerReq);

        // Follow first, then unfollow
        controller.followUser("followed", followerReq);
        assertDoesNotThrow(() -> controller.unfollowUser("followed", followerReq));
    }

    @Test
    public void testUnfollowNonExistentUser() {
        controller.register(basic("follower", "pass1"));
        HttpServletRequest followerReq = mockRequest("follower", "pass1");
        controller.login(followerReq);

        assertThrows(ResponseStatusException.class,
                () -> controller.unfollowUser("nonexistent", followerReq));
    }

    @Test
    public void testUnfollowNotFollowedUser() {
        controller.register(basic("follower", "pass1"));
        controller.register(basic("followed", "pass2"));
        
        HttpServletRequest followerReq = mockRequest("follower", "pass1");
        controller.login(followerReq);

        // Try to unfollow without following first
        assertThrows(ResponseStatusException.class,
                () -> controller.unfollowUser("followed", followerReq));
    }

    @Test
    public void testRegisterWithInvalidAuthHeader() {
        assertThrows(ResponseStatusException.class,
                () -> controller.register("InvalidHeader"));
    }

    @Test
    public void testRegisterWithNullAuthHeader() {
        assertThrows(ResponseStatusException.class,
                () -> controller.register(null));
    }

    @Test
    public void testRegisterDuplicateUser() {
        String username = "duplicateUser";
        String password = "pass123";
        
        // Register first time
        controller.register(basic(username, password));
        
        // Try to register again with same username
        assertThrows(ResponseStatusException.class,
                () -> controller.register(basic(username, password)));
    }

    @Test
    public void testLoginWithWrongPassword() {
        String username = "wrongPassUser";
        String password = "correctPass";
        
        controller.register(basic(username, password));
        
        assertThrows(ResponseStatusException.class,
                () -> controller.login(mockRequest(username, "wrongPass")));
    }

    @Test
    public void testLoginNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> controller.login(mockRequest("nonexistent", "pass")));
    }

    @Test
    public void testSearchUsersWithoutAuthentication() {
        HttpServletRequest unauthenticatedReq = new MockHttpServletRequest();
        // Should not throw; should return results (possibly not personalized)
        assertDoesNotThrow(() -> controller.searchUsers("test", unauthenticatedReq));
    }

    @Test
    public void testGetUserWithoutAuthentication() {
        HttpServletRequest unauthenticatedReq = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class,
                () -> controller.getUser("testuser1"));
    }

    @Test
    public void testFollowUserWithoutAuthentication() {
        HttpServletRequest unauthenticatedReq = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class,
                () -> controller.followUser("testuser2", unauthenticatedReq));
    }

    @Test
    public void testUnfollowUserWithoutAuthentication() {
        HttpServletRequest unauthenticatedReq = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class,
                () -> controller.unfollowUser("testuser2", unauthenticatedReq));
    }

    @Test
    public void testDeleteUserWithPosts() {
        String username = "deleteTestUser";
        String password = "testPass";

        controller.register(basic(username, password));
        controller.login(mockRequest(username, password));
        postController.createPost(new CreatePostDto("post1"), mockRequest(username, password));
        assertDoesNotThrow(() -> userRepository.deleteById(username));
    }
}
