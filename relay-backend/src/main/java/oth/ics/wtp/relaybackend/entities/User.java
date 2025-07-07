package oth.ics.wtp.relaybackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.EAGER;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "relay_users")
public class User {

    @Id
    @Column(length = 50, nullable = false)
    private String username;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = true, length = 100)
    private String fullName;

    @Column(nullable = true, length = 100)
    private String email;

    @Column(nullable = true, length = 500)
    private String biography;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @OneToMany(mappedBy = "author", fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @JsonIgnore
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


    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public List<Post> getPosts() { return posts; }

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