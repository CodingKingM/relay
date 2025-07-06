package oth.ics.wtp.relaybackend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.WeakCrypto;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.repositories.UserRepository;
import oth.ics.wtp.relaybackend.repositories.FollowRepository;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FollowServiceTest {
    @Autowired private FollowService followService;
    @Autowired private UserRepository userRepository;
    @Autowired private FollowRepository followRepository;

    @BeforeEach
    public void setup() {
        userRepository.save(new User("user1", WeakCrypto.hashPassword("pass1")));
        userRepository.save(new User("user2", WeakCrypto.hashPassword("pass2")));
        userRepository.save(new User("user3", WeakCrypto.hashPassword("pass3")));
    }

    @Test
    public void testFollowUser() {
        followService.followUser("user1", "user2");

        assertTrue(followService.isFollowing("user1", "user2"));
        assertFalse(followService.isFollowing("user2", "user1"));

        assertEquals(1, followRepository.countByFollowerUsername("user1"));
        assertEquals(1, followRepository.countByFollowedUsername("user2"));
    }

    @Test
    public void testCannotFollowSelf() {
        assertThrows(ResponseStatusException.class,
                () -> followService.followUser("user1", "user1"));
    }

    @Test
    public void testCannotFollowTwice() {
        followService.followUser("user1", "user2");
        assertThrows(ResponseStatusException.class,
                () -> followService.followUser("user1", "user2"));
    }

    @Test
    public void testFollowNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> followService.followUser("user1", "nonexistent"));
        assertThrows(ResponseStatusException.class,
                () -> followService.followUser("nonexistent", "user1"));
    }

    @Test
    public void testUnfollowUser() {
        followService.followUser("user1", "user2");
        assertTrue(followService.isFollowing("user1", "user2"));

        followService.unfollowUser("user1", "user2");
        assertFalse(followService.isFollowing("user1", "user2"));

        assertEquals(0, followRepository.countByFollowerUsername("user1"));
        assertEquals(0, followRepository.countByFollowedUsername("user2"));
    }

    @Test
    public void testUnfollowNotFollowedUser() {
        assertThrows(ResponseStatusException.class,
                () -> followService.unfollowUser("user1", "user2"));
    }

    @Test
    public void testMultipleFollowRelationships() {
        followService.followUser("user1", "user2");
        followService.followUser("user1", "user3");
        followService.followUser("user2", "user3");

        assertEquals(2, followRepository.countByFollowerUsername("user1"));
        assertEquals(0, followRepository.countByFollowedUsername("user1"));
        assertEquals(1, followRepository.countByFollowerUsername("user2"));
        assertEquals(1, followRepository.countByFollowedUsername("user2"));
        assertEquals(0, followRepository.countByFollowerUsername("user3"));
        assertEquals(2, followRepository.countByFollowedUsername("user3"));
    }

    @Test
    public void testIsFollowing() {
        assertFalse(followService.isFollowing("user1", "user2"));

        followService.followUser("user1", "user2");
        assertTrue(followService.isFollowing("user1", "user2"));
        assertFalse(followService.isFollowing("user2", "user1"));
    }

    @Test
    public void testIsFollowingNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> followService.isFollowing("user1", "nonexistent"));
        assertThrows(ResponseStatusException.class,
                () -> followService.isFollowing("nonexistent", "user1"));
    }
} 