package oth.ics.wtp.relaybackend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.dtos.*;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.repositories.UserRepository;
import oth.ics.wtp.relaybackend.repositories.FollowRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserServiceTest {
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private FollowService followService;
    @Autowired private FollowRepository followRepository;

    @BeforeEach
    public void setup() {
        // Clean up all data first
        followRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testCreateUser() {
        CreateUserDto dto = new CreateUserDto("newuser123", "password123");
        UserDto created = userService.createUser(dto);

        assertEquals("newuser123", created.username());
        assertNotNull(created.registeredAt());
        assertEquals(0, created.followerCount());
        assertEquals(0, created.followingCount());

        assertTrue(userRepository.existsByUsername("newuser123"));
    }

    @Test
    public void testCreateDuplicateUser() {
        CreateUserDto dto = new CreateUserDto("duplicate123", "pass");
        userService.createUser(dto);

        assertThrows(ResponseStatusException.class,
                () -> userService.createUser(dto));
    }

    @Test
    public void testGetUserByUsername() {
        CreateUserDto dto = new CreateUserDto("gettest123", "pass");
        userService.createUser(dto);

        UserDto retrieved = userService.getUserByUsername("gettest123");
        assertEquals("gettest123", retrieved.username());
    }

    @Test
    public void testGetNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> userService.getUserByUsername("nonexistent"));
    }

    @Test
    public void testSearchUsers() {
        userService.createUser(new CreateUserDto("alice123", "pass"));
        userService.createUser(new CreateUserDto("alex123", "pass"));
        userService.createUser(new CreateUserDto("bob123", "pass"));

        List<UserSearchDto> results = userService.searchUsers("al", null);
        assertEquals(2, results.size());

        results = userService.searchUsers("b", null);
        assertEquals(1, results.size());
        assertEquals("bob123", results.get(0).username());
    }

    @Test
    public void testSearchUsersWithFollowStatus() {
        userService.createUser(new CreateUserDto("searcher123", "pass"));
        userService.createUser(new CreateUserDto("target123", "pass"));

        followService.followUser("searcher123", "target123");

        List<UserSearchDto> results = userService.searchUsers("target", "searcher123");
        assertEquals(1, results.size());
        assertTrue(results.get(0).isFollowing());
    }

    @Test
    public void testDeleteUser() {
        userService.createUser(new CreateUserDto("todelete123", "pass"));
        assertTrue(userService.userExists("todelete123"));

        userService.deleteUser("todelete123");
        assertFalse(userService.userExists("todelete123"));
    }

    @Test
    public void testDeleteNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> userService.deleteUser("nonexistent"));
    }

    @Test
    public void testUserDtoIncludesFollowerCounts() {
        userService.createUser(new CreateUserDto("popular123", "pass"));
        userService.createUser(new CreateUserDto("follower1123", "pass"));
        userService.createUser(new CreateUserDto("follower2123", "pass"));

        followService.followUser("follower1123", "popular123");
        followService.followUser("follower2123", "popular123");
        followService.followUser("popular123", "follower1123");

        UserDto dto = userService.getUserByUsername("popular123");
        assertEquals(2, dto.followerCount());
        assertEquals(1, dto.followingCount());
    }
}