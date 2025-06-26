package oth.ics.wtp.relaybackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "relay_users")
public class User {

    @Id
    @Column(length = 50, nullable = false)
    private String username;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @OneToMany(mappedBy = "author", fetch = LAZY, cascade = ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @ManyToMany(fetch = LAZY)
    @JoinTable(
            name = "user_follows",
            joinColumns = @JoinColumn(name = "follower_username"),
            inverseJoinColumns = @JoinColumn(name = "followed_username")
    )
    private List<User> following = new ArrayList<>();

    @ManyToMany(mappedBy = "following", fetch = LAZY)
    private List<User> followers = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = LAZY, cascade = ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    public User() {
        this.registeredAt = LocalDateTime.now();
    }

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.registeredAt = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<User> getFollowing() {
        return following;
    }

    public void setFollowing(List<User> following) {
        this.following = following;
    }

    public List<User> getFollowers() {
        return followers;
    }

    public void setFollowers(List<User> followers) {
        this.followers = followers;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public void addFollower(User follower) {
        if (!this.followers.contains(follower)) {
            this.followers.add(follower);
            follower.getFollowing().add(this);
        }
    }

    public void removeFollower(User follower) {
        this.followers.remove(follower);
        follower.getFollowing().remove(this);
    }
}