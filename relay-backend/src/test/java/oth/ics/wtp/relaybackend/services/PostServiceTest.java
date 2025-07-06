package oth.ics.wtp.relaybackend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.WeakCrypto;
import oth.ics.wtp.relaybackend.dtos.CreatePostDto;
import oth.ics.wtp.relaybackend.dtos.PostDto;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.repositories.UserRepository;
import oth.ics.wtp.relaybackend.repositories.PostRepository;
import oth.ics.wtp.relaybackend.repositories.LikeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PostServiceTest {
    @Autowired private PostService postService;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private LikeRepository likeRepository;

    @BeforeEach
    public void setup() {
        // Clean up all data first
        likeRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create fresh users
        userRepository.save(new User("postuser1", WeakCrypto.hashPassword("pass1")));
        userRepository.save(new User("postuser2", WeakCrypto.hashPassword("pass2")));
        userRepository.save(new User("postuser3", WeakCrypto.hashPassword("pass3")));
    }

    @Test
    public void testCreatePost() {
        CreatePostDto dto = new CreatePostDto("Test post content");
        PostDto created = postService.createPost(dto, "postuser1");

        assertEquals("Test post content", created.content());
        assertEquals("postuser1", created.authorUsername());
        assertEquals(0, created.likeCount());
        assertFalse(created.isLikedByCurrentUser());
    }

    @Test
    public void testCreatePostWithNullContent() {
        CreatePostDto dto = new CreatePostDto(null);
        assertThrows(ResponseStatusException.class,
                () -> postService.createPost(dto, "postuser1"));
    }

    @Test
    public void testCreatePostWithEmptyContent() {
        CreatePostDto dto = new CreatePostDto("");
        assertThrows(ResponseStatusException.class,
                () -> postService.createPost(dto, "postuser1"));
    }

    @Test
    public void testCreatePostWithWhitespaceContent() {
        CreatePostDto dto = new CreatePostDto("   ");
        assertThrows(ResponseStatusException.class,
                () -> postService.createPost(dto, "postuser1"));
    }

    @Test
    public void testCreatePostWithContentTooLong() {
        String longContent = "a".repeat(281); // 281 characters, exceeding 280 limit
        CreatePostDto dto = new CreatePostDto(longContent);
        assertThrows(ResponseStatusException.class,
                () -> postService.createPost(dto, "postuser1"));
    }

    @Test
    public void testCreatePostWithContentExactly280Characters() {
        String exactContent = "a".repeat(280); // Exactly 280 characters
        CreatePostDto dto = new CreatePostDto(exactContent);
        assertDoesNotThrow(() -> postService.createPost(dto, "postuser1"));
    }

    @Test
    public void testCreatePostWithNonExistentUser() {
        CreatePostDto dto = new CreatePostDto("Test post");
        assertThrows(ResponseStatusException.class,
                () -> postService.createPost(dto, "nonexistent"));
    }

    @Test
    public void testGetUserPosts() {
        // Create posts
        postService.createPost(new CreatePostDto("Post 1"), "postuser1");
        postService.createPost(new CreatePostDto("Post 2"), "postuser1");
        postService.createPost(new CreatePostDto("Post 3"), "postuser2");

        List<PostDto> user1Posts = postService.getUserPosts("postuser1", "postuser2");
        assertEquals(2, user1Posts.size());
        assertEquals("Post 2", user1Posts.get(0).content()); // Most recent first
        assertEquals("Post 1", user1Posts.get(1).content());
    }

    @Test
    public void testGetUserPostsForNonExistentUser() {
        assertThrows(ResponseStatusException.class,
                () -> postService.getUserPosts("nonexistent", "postuser1"));
    }

    @Test
    public void testGetUserPostsWithNullCurrentUser() {
        postService.createPost(new CreatePostDto("Test post"), "postuser1");
        List<PostDto> posts = postService.getUserPosts("postuser1", null);
        assertEquals(1, posts.size());
        assertFalse(posts.get(0).isLikedByCurrentUser());
    }

    @Test
    public void testGetTimelinePosts() {
        // Create posts
        postService.createPost(new CreatePostDto("User1 post"), "postuser1");
        postService.createPost(new CreatePostDto("User2 post"), "postuser2");

        List<PostDto> timeline = postService.getTimelinePosts("postuser1");
        // Timeline should be empty since user1 doesn't follow anyone
        assertEquals(0, timeline.size());
    }

    @Test
    public void testLikePost() {
        PostDto post = postService.createPost(new CreatePostDto("Test post"), "postuser1");
        
        PostDto likedPost = postService.likePost(post.id(), "postuser2");
        assertEquals(1, likedPost.likeCount());
        assertTrue(likedPost.isLikedByCurrentUser());
    }

    @Test
    public void testLikeOwnPost() {
        PostDto post = postService.createPost(new CreatePostDto("Test post"), "postuser1");
        
        assertThrows(ResponseStatusException.class,
                () -> postService.likePost(post.id(), "postuser1"));
    }

    @Test
    public void testLikeNonExistentPost() {
        assertThrows(ResponseStatusException.class,
                () -> postService.likePost(99999L, "postuser2"));
    }

    @Test
    public void testLikePostWithNonExistentUser() {
        PostDto post = postService.createPost(new CreatePostDto("Test post"), "postuser1");
        
        assertThrows(ResponseStatusException.class,
                () -> postService.likePost(post.id(), "nonexistent"));
    }

    @Test
    public void testLikePostTwice() {
        PostDto post = postService.createPost(new CreatePostDto("Test post"), "postuser1");
        
        postService.likePost(post.id(), "postuser2");
        assertThrows(ResponseStatusException.class,
                () -> postService.likePost(post.id(), "postuser2"));
    }

    @Test
    public void testUnlikePost() {
        PostDto post = postService.createPost(new CreatePostDto("Test post"), "postuser1");
        
        // Like first
        postService.likePost(post.id(), "postuser2");
        
        // Then unlike
        assertDoesNotThrow(() -> postService.unlikePost(post.id(), "postuser2"));
    }

    @Test
    public void testUnlikeNonExistentPost() {
        assertThrows(ResponseStatusException.class,
                () -> postService.unlikePost(99999L, "postuser2"));
    }

    @Test
    public void testUnlikePostWithNonExistentUser() {
        PostDto post = postService.createPost(new CreatePostDto("Test post"), "postuser1");
        
        assertThrows(ResponseStatusException.class,
                () -> postService.unlikePost(post.id(), "nonexistent"));
    }

    @Test
    public void testUnlikePostNotLiked() {
        PostDto post = postService.createPost(new CreatePostDto("Test post"), "postuser1");
        
        // Try to unlike without liking first - this should not throw an exception
        // because the unlike operation just removes the like if it exists
        assertDoesNotThrow(() -> postService.unlikePost(post.id(), "postuser2"));
    }

    @Test
    public void testPostDtoWithLikeStatus() {
        PostDto post = postService.createPost(new CreatePostDto("Test post"), "postuser1");
        
        // Check when current user is the author
        PostDto authorView = postService.getUserPosts("postuser1", "postuser1").get(0);
        assertFalse(authorView.isLikedByCurrentUser());
        
        // Check when current user is different
        PostDto otherView = postService.getUserPosts("postuser1", "postuser2").get(0);
        assertFalse(otherView.isLikedByCurrentUser());
        
        // Like the post
        postService.likePost(post.id(), "postuser2");
        
        // Check again
        PostDto likedView = postService.getUserPosts("postuser1", "postuser2").get(0);
        assertTrue(likedView.isLikedByCurrentUser());
        assertEquals(1, likedView.likeCount());
    }
} 