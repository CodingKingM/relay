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
    @Column(name = "user_username", nullable = false)
    private String user;

    @Id
    @Column(name = "post_id", nullable = false)
    private Long post;

    @ManyToOne(fetch = EAGER, optional = false)
    @JoinColumn(name = "user_username", referencedColumnName = "username", insertable = false, updatable = false)
    private User userEntity;

    @ManyToOne(fetch = EAGER, optional = false)
    @JoinColumn(name = "post_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Post postEntity;

    @Column(nullable = false)
    private LocalDateTime likedAt;

    public Like() {
        this.likedAt = LocalDateTime.now();
    }

    public Like(User userEntity, Post postEntity) {
        this.user = userEntity.getUsername();
        this.post = postEntity.getId();
        this.userEntity = userEntity;
        this.postEntity = postEntity;
        this.likedAt = LocalDateTime.now();
        System.out.println("DEBUG: Creating Like with user=" + this.user + ", post=" + this.post);
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

    public User getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(User userEntity) {
        this.userEntity = userEntity;
    }

    public Post getPostEntity() {
        return postEntity;
    }

    public void setPostEntity(Post postEntity) {
        this.postEntity = postEntity;
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