package oth.ics.wtp.relaybackend.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import static jakarta.persistence.FetchType.EAGER;

@Entity
@Table(name = "user_follows",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_username", "followed_username"}))
@IdClass(Follow.FollowId.class)
public class Follow {

    @Id
    @ManyToOne(fetch = EAGER, optional = false)
    @JoinColumn(name = "follower_username", nullable = false)
    private User follower;

    @Id
    @ManyToOne(fetch = EAGER, optional = false)
    @JoinColumn(name = "followed_username", nullable = false)
    private User followed;

    @Column(nullable = true)
    private LocalDateTime followedAt;

    public Follow() {
        this.followedAt = LocalDateTime.now();
    }

    public Follow(User follower, User followed) {
        this.follower = follower;
        this.followed = followed;
        this.followedAt = LocalDateTime.now();
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollowed() {
        return followed;
    }

    public void setFollowed(User followed) {
        this.followed = followed;
    }

    public LocalDateTime getFollowedAt() {
        return followedAt;
    }

    public void setFollowedAt(LocalDateTime followedAt) {
        this.followedAt = followedAt;
    }

    public static class FollowId implements Serializable {
        private String follower;
        private String followed;



        public String getFollower() {
            return follower;
        }

        public void setFollower(String follower) {
            this.follower = follower;
        }

        public String getFollowed() {
            return followed;
        }

        public void setFollowed(String followed) {
            this.followed = followed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FollowId followId = (FollowId) o;
            return Objects.equals(follower, followId.follower) &&
                    Objects.equals(followed, followId.followed);
        }

        @Override
        public int hashCode() {
            return Objects.hash(follower, followed);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Follow follow = (Follow) o;
        return Objects.equals(follower, follow.follower) &&
                Objects.equals(followed, follow.followed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(follower, followed);
    }
}