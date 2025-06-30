package oth.ics.wtp.relaybackend.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.dtos.CreatePostDto;
import oth.ics.wtp.relaybackend.dtos.PostDto;
import oth.ics.wtp.relaybackend.entities.Like;
import oth.ics.wtp.relaybackend.entities.Post;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.repositories.LikeRepository;
import oth.ics.wtp.relaybackend.repositories.PostRepository;
import oth.ics.wtp.relaybackend.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
    }

    public PostDto createPost(CreatePostDto createPostDto, String username) {
        User author = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (createPostDto.content() == null || createPostDto.content().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post content cannot be empty");
        }

        if (createPostDto.content().length() > 280) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post content exceeds 280 characters");
        }

        Post post = new Post(createPostDto.content().trim(), author);
        Post savedPost = postRepository.save(post);

        return toDto(savedPost, username);
    }

    public List<PostDto> getUserPosts(String username, String currentUsername) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Pageable pageable = PageRequest.of(0, 20);
        return postRepository.findByAuthorUsernameOrderByCreatedAtDesc(username, pageable)
                .stream()
                .map(post -> toDto(post, currentUsername))
                .collect(Collectors.toList());
    }

    public List<PostDto> getTimelinePosts(String username) {
        Pageable pageable = PageRequest.of(0, 20);
        List<Post> posts = postRepository.findFollowedUsersPosts(username, pageable);

        return posts.stream()
                .map(post -> toDto(post, username))
                .collect(Collectors.toList());
    }

    public PostDto likePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (post.getAuthor().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot like your own post");
        }

        if (likeRepository.existsByUserUsernameAndPostId(username, postId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Post already liked");
        }

        Like like = new Like(user, post);
        likeRepository.save(like);

        return toDto(post, username);
    }

    public void unlikePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Like.LikeId likeId = new Like.LikeId(username, postId);
        likeRepository.deleteById(likeId);
    }

    private PostDto toDto(Post post, String currentUsername) {
        boolean isLiked = false;
        if (currentUsername != null) {
            isLiked = post.isLikedByUser(currentUsername);
        }

        return new PostDto(
                post.getId(),
                post.getContent(),
                post.getAuthor().getUsername(),
                post.getCreatedAt(),
                post.getLikeCount(),
                isLiked
        );
    }
}