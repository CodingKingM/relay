package oth.ics.wtp.relaybackend.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.dtos.*;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.repositories.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserServiceTest {
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private FollowService followService;

    @Test
    public void testCreateUser() {
        CreateUserDto dto = new CreateUserDto("newuser", "password123");
        UserDto created = userService.createUser(dto);

        assertEquals("newuser", created.username());
        assertNotNull(created.registeredAt());
        assertEquals(0, created.followerCount());
        assertEquals(0, created.followingCount());

        assertTrue(userRepository.existsByUsername("newuser"));
    }

    @Test
    public void testCreateDuplicateUser() {
        CreateUserDto dto = new CreateUserDto("duplicate", "pass");
        userService.createUser(dto);

        assertThrows(ResponseStatusException.class,
                () -> userService.createUser(dto));
    }

    @Test
    public void testGetUserByUsername() {
        CreateUserDto dto = new CreateUserDto("gettest", "pass");
        userService.createUser(dto);

        UserDto retrieved = userService.getUserByUsername("gettest");
        assertEquals("gettest", retrieved.username());
    }

    @Test
    public void testGetNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> userService.getUserByUsername("nonexistent"));
    }

    @Test
    public void testSearchUsers() {
        userService.createUser(new CreateUserDto("alice", "pass"));
        userService.createUser(new CreateUserDto("alex", "pass"));
        userService.createUser(new CreateUserDto("bob", "pass"));

        List<UserSearchDto> results = userService.searchUsers("al", null);
        assertEquals(2, results.size());

        results = userService.searchUsers("b", null);
        assertEquals(1, results.size());
        assertEquals("bob", results.get(0).username());
    }

    @Test
    public void testSearchUsersWithFollowStatus() {
        userService.createUser(new CreateUserDto("searcher", "pass"));
        userService.createUser(new CreateUserDto("target", "pass"));

        followService.followUser("searcher", "target");

        List<UserSearchDto> results = userService.searchUsers("target", "searcher");
        assertEquals(1, results.size());
        assertTrue(results.get(0).isFollowing());
    }

    @Test
    public void testDeleteUser() {
        userService.createUser(new CreateUserDto("todelete", "pass"));
        assertTrue(userService.userExists("todelete"));

        userService.deleteUser("todelete");
        assertFalse(userService.userExists("todelete"));
    }

    @Test
    public void testDeleteNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> userService.deleteUser("nonexistent"));
    }

    @Test
    public void testUserDtoIncludesFollowerCounts() {
        userService.createUser(new CreateUserDto("popular", "pass"));
        userService.createUser(new CreateUserDto("follower1", "pass"));
        userService.createUser(new CreateUserDto("follower2", "pass"));

        followService.followUser("follower1", "popular");
        followService.followUser("follower2", "popular");
        followService.followUser("popular", "follower1");

        UserDto dto = userService.getUserByUsername("popular");
        assertEquals(2, dto.followerCount());
        assertEquals(1, dto.followingCount());
    }
}