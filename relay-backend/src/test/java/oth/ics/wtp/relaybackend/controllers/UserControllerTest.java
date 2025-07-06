package oth.ics.wtp.relaybackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.dtos.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest extends RelayControllerTestBase {

    @Autowired private UserController controller;
    @Autowired private PostController postController;

    @Test
    public void testRegisterAndLogin() {
        String username = "newuser";
        String password = "password123";
        
        // Register user
        UserDto registered = controller.register(basic(username, password));
        assertEquals(username, registered.username());
        
        // Login user
        UserDto loggedIn = controller.login(mockRequest(username, password));
        assertEquals(username, loggedIn.username());
        
        // Logout user
        controller.logout(mockRequest(username, password));
    }

    @Test
    public void testGetUserByUsername() {
        UserDto user = controller.getUser(USER1_USERNAME);
        assertEquals(USER1_USERNAME, user.username());
    }

    @Test
    public void testSearchUsers() {
        controller.register(basic("alice", "pass1"));
        controller.register(basic("alex", "pass2"));
        
        List<UserSearchDto> results = controller.searchUsers("al", user1());
        assertTrue(results.size() >= 2);
    }

    @Test
    public void testFollowAndUnfollowUser() {
        controller.register(basic("follower", "pass1"));
        controller.register(basic("followed", "pass2"));
        
        HttpServletRequest followerReq = mockRequest("follower", "pass1");
        controller.login(followerReq);
        
        // Follow user
        controller.followUser("followed", followerReq);
        
        // Unfollow user
        controller.unfollowUser("followed", followerReq);
    }

    @Test
    public void testGetCurrentUserProfile() {
        controller.login(user1());
        UserDto profile = controller.getCurrentUserProfile(user1());
        assertEquals(USER1_USERNAME, profile.username());
    }

    @Test
    public void testUpdateCurrentUserProfile() {
        controller.login(user1());
        
        UserDto updateDto = new UserDto(
            USER1_USERNAME,
            null,
            0,
            0,
            "Updated Name",
            "updated@example.com",
            "Updated bio"
        );
        
        UserDto updated = controller.updateCurrentUserProfile(updateDto, user1());
        assertEquals("Updated Name", updated.fullName());
        assertEquals("updated@example.com", updated.email());
        assertEquals("Updated bio", updated.biography());
    }

    @Test
    public void testGetFollowersAndFollowing() {
        controller.register(basic("follower", "pass1"));
        controller.register(basic("followed", "pass2"));
        
        HttpServletRequest followerReq = mockRequest("follower", "pass1");
        controller.login(followerReq);
        controller.followUser("followed", followerReq);
        
        // Get followers
        var followers = controller.getFollowers("followed");
        assertEquals(1, followers.size());
        
        // Get following
        var following = controller.getFollowing("follower");
        assertEquals(1, following.size());
    }

    @Test
    public void testLoginWithWrongPassword() {
        String username = "wrongpassuser";
        String password = "correctpass";
        
        controller.register(basic(username, password));
        
        assertThrows(ResponseStatusException.class,
                () -> controller.login(mockRequest(username, "wrongpass")));
    }

    @Test
    public void testRegisterDuplicateUser() {
        String username = "duplicateuser";
        String password = "pass123";
        
        // Register first time
        controller.register(basic(username, password));
        
        // Try to register again with same username
        assertThrows(ResponseStatusException.class,
                () -> controller.register(basic(username, password)));
    }
}
