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

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FollowServiceTest {
    @Autowired private FollowService followService;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        userRepository.save(new User("followuser1", WeakCrypto.hashPassword("pass1")));
        userRepository.save(new User("followuser2", WeakCrypto.hashPassword("pass2")));
        userRepository.save(new User("followuser3", WeakCrypto.hashPassword("pass3")));
    }

    @Test
    public void testFollowUser() {
        followService.followUser("followuser1", "followuser2");

        assertTrue(followService.isFollowing("followuser1", "followuser2"));
        assertFalse(followService.isFollowing("followuser2", "followuser1"));

        User user1 = userRepository.findById("followuser1").get();
        User user2 = userRepository.findById("followuser2").get();

        assertEquals(1, user1.getFollowing().size());
        assertEquals(1, user2.getFollowers().size());
    }

    @Test
    public void testCannotFollowSelf() {
        assertThrows(ResponseStatusException.class,
                () -> followService.followUser("followuser1", "followuser1"));
    }

    @Test
    public void testCannotFollowTwice() {
        followService.followUser("followuser1", "followuser2");
        assertThrows(ResponseStatusException.class,
                () -> followService.followUser("followuser1", "followuser2"));
    }

    @Test
    public void testFollowNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> followService.followUser("followuser1", "nonexistent"));
        assertThrows(ResponseStatusException.class,
                () -> followService.followUser("nonexistent", "followuser1"));
    }

    @Test
    public void testUnfollowUser() {
        followService.followUser("followuser1", "followuser2");
        assertTrue(followService.isFollowing("followuser1", "followuser2"));

        followService.unfollowUser("followuser1", "followuser2");
        assertFalse(followService.isFollowing("followuser1", "followuser2"));

        User user1 = userRepository.findById("followuser1").get();
        User user2 = userRepository.findById("followuser2").get();

        assertEquals(0, user1.getFollowing().size());
        assertEquals(0, user2.getFollowers().size());
    }

    @Test
    public void testUnfollowNotFollowedUser() {
        assertThrows(ResponseStatusException.class,
                () -> followService.unfollowUser("followuser1", "followuser2"));
    }

    @Test
    public void testUnfollowNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> followService.unfollowUser("followuser1", "nonexistent"));
        assertThrows(ResponseStatusException.class,
                () -> followService.unfollowUser("nonexistent", "followuser1"));
    }

    @Test
    public void testMultipleFollowRelationships() {
        followService.followUser("followuser1", "followuser2");
        followService.followUser("followuser1", "followuser3");
        followService.followUser("followuser2", "followuser3");

        User user1 = userRepository.findById("followuser1").get();
        User user2 = userRepository.findById("followuser2").get();
        User user3 = userRepository.findById("followuser3").get();

        assertEquals(2, user1.getFollowing().size());
        assertEquals(0, user1.getFollowers().size());
        assertEquals(1, user2.getFollowing().size());
        assertEquals(1, user2.getFollowers().size());
        assertEquals(0, user3.getFollowing().size());
        assertEquals(2, user3.getFollowers().size());
    }

    @Test
    public void testIsFollowing() {
        assertFalse(followService.isFollowing("followuser1", "followuser2"));

        followService.followUser("followuser1", "followuser2");
        assertTrue(followService.isFollowing("followuser1", "followuser2"));
        assertFalse(followService.isFollowing("followuser2", "followuser1"));
    }

    @Test
    public void testIsFollowingNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> followService.isFollowing("followuser1", "nonexistent"));
        assertThrows(ResponseStatusException.class,
                () -> followService.isFollowing("nonexistent", "followuser1"));
    }
}