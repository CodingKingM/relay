package oth.ics.wtp.relaybackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;
import java.util.Objects;

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

    @OneToMany(mappedBy = "author", fetch = EAGER, cascade = ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @ManyToMany(fetch = EAGER, cascade = {PERSIST, MERGE})
    @JoinTable(
            name = "user_follows",
            joinColumns = @JoinColumn(name = "follower_username"),
            inverseJoinColumns = @JoinColumn(name = "followed_username"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"follower_username", "followed_username"})
    )
    private List<User> following = new ArrayList<>();

    @ManyToMany(mappedBy = "following", fetch = EAGER)
    private List<User> followers = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = EAGER, cascade = ALL, orphanRemoval = true)
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
    
    public String getHashedPassword() {
        return hashedPassword;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public List<User> getFollowing() {
        return following;
    }

    public List<User> getFollowers() {
        return followers;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    public void setUsername(String username) {
        this.username = username;
    }
}