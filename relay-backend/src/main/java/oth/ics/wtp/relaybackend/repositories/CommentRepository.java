package oth.ics.wtp.relaybackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import oth.ics.wtp.relaybackend.entities.Comment;
import oth.ics.wtp.relaybackend.entities.Post;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
} 