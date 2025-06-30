package oth.ics.wtp.relaybackend.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import static jakarta.persistence.FetchType.EAGER;

@Entity
@Table(name = "likes")
@IdClass(Like.LikeId.class)
public class Like {

    @Id
    @ManyToOne(fetch = EAGER, optional = false)
    @JoinColumn(name = "user_username", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = EAGER, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private LocalDateTime likedAt;

    public Like() {
        this.likedAt = LocalDateTime.now();
    }

    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
        this.likedAt = LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public LocalDateTime getLikedAt() {
        return likedAt;
    }

    public void setLikedAt(LocalDateTime likedAt) {
        this.likedAt = likedAt;
    }

    public static class LikeId implements Serializable {
        private String user;
        private Long post;

        public LikeId() {}

        public LikeId(String user, Long post) {
            this.user = user;
            this.post = post;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public Long getPost() {
            return post;
        }

        public void setPost(Long post) {
            this.post = post;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LikeId likeId = (LikeId) o;
            return Objects.equals(user, likeId.user) &&
                    Objects.equals(post, likeId.post);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, post);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Like like = (Like) o;
        return Objects.equals(user, like.user) &&
                Objects.equals(post, like.post);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, post);
    }
}