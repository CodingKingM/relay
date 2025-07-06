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
import oth.ics.wtp.relaybackend.entities.Comment;
import oth.ics.wtp.relaybackend.repositories.LikeRepository;
import oth.ics.wtp.relaybackend.repositories.PostRepository;
import oth.ics.wtp.relaybackend.repositories.UserRepository;
import oth.ics.wtp.relaybackend.repositories.CommentRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, LikeRepository likeRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
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

        if (likeRepository.existsByUserAndPost(username, postId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already liked");
        }
        Like like = new Like(user, post);
        likeRepository.save(like);
        postRepository.findById(postId); // Reload post to update like count

        return toDto(post, username);
    }

    @Transactional
    public void unlikePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        System.out.println("DEBUG: Attempting to delete like for user='" + username + "', postId=" + postId);
        System.out.println("DEBUG: Likes for post before delete:");
        likeRepository.findAll().stream()
            .filter(l -> l.getPost().equals(postId))
            .forEach(l -> System.out.println("  Like: user='" + l.getUser() + "', post=" + l.getPost()));

        likeRepository.deleteByUserAndPost(username, postId);
        likeRepository.flush();
        postRepository.findById(postId); // Reload post to update like count
        System.out.println("DEBUG: Likes for post after delete:");
        likeRepository.findAll().stream()
            .filter(l -> l.getPost().equals(postId))
            .forEach(l -> System.out.println("  Like: user='" + l.getUser() + "', post=" + l.getPost()));
        System.out.println("DEBUG: Like count after delete: " + likeRepository.countByPost(postId));
    }

    @Transactional
    public void deletePost(Long postId, String username) {
        System.out.println("DEBUG: Looking up post id=" + postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own posts");
        }
        // Remove post from author's posts list and save user (orphanRemoval will handle the rest)
        User author = post.getAuthor();
        author.getPosts().removeIf(p -> p.getId().equals(postId));
        userRepository.save(author);
        System.out.println("DEBUG: Orphan removal should delete post id=" + postId);
    }

    public List<Comment> getCommentsForPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        return commentRepository.findByPostOrderByCreatedAtAsc(post);
    }

    public Comment addCommentToPost(Long postId, String username, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment content cannot be empty");
        }
        if (content.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment exceeds 500 characters");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Comment comment = new Comment(post, user, content.trim());
        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!comment.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    private PostDto toDto(Post post, String currentUsername) {
        boolean isLiked = false;
        if (currentUsername != null) {
            isLiked = likeRepository.existsByUserAndPost(currentUsername, post.getId());
        }
        int likeCount = likeRepository.countByPost(post.getId());
        return new PostDto(
                post.getId(),
                post.getContent(),
                post.getAuthor().getUsername(),
                post.getCreatedAt(),
                likeCount,
                isLiked
        );
    }
}