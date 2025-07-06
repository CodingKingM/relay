package oth.ics.wtp.relaybackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import oth.ics.wtp.relaybackend.entities.Comment;
import oth.ics.wtp.relaybackend.entities.Post;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
    
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post = :post")
    void deleteByPost(@Param("post") Post post);
} 