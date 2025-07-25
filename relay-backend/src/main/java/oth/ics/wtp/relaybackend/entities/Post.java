package oth.ics.wtp.relaybackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import java.util.Objects;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_created", columnList = "createdAt DESC"),
        @Index(name = "idx_post_author", columnList = "author_username")
})
public class Post {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, length = 280)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = EAGER, optional = false)
    @JoinColumn(name = "author_username", nullable = false)
    private User author;

    @OneToMany(mappedBy = "post", fetch = EAGER, cascade = ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = ALL, orphanRemoval = true, fetch = EAGER)
    private List<Comment> comments = new ArrayList<>();

    public Post() {
        this.createdAt = LocalDateTime.now();
    }

    public Post(String content, User author) {
        this.content = content;
        this.author = author;
        this.createdAt = LocalDateTime.now();
    }

    public int getLikeCount() {
        return likes.size();
    }

    public boolean isLikedByUser(String username) {
        return likes.stream()
                .anyMatch(like -> like.getUser().equals(username));
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getAuthor() {
        return author;
    }

    public void addLike(Like like) {
        likes.add(like);
        like.setPost(this.id);
    }

    public void removeLike(Like like) {
        likes.remove(like);
        like.setPost(null);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<Like> getLikes() {
        return likes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}