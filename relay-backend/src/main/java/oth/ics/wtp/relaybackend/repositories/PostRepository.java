package oth.ics.wtp.relaybackend.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import oth.ics.wtp.relaybackend.entities.Post;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.author IN " +
            "(SELECT u.following FROM User u WHERE u.username = :username) " +
            "ORDER BY p.createdAt DESC")
    List<Post> findFollowedUsersPosts(@Param("username") String username, Pageable pageable);
}