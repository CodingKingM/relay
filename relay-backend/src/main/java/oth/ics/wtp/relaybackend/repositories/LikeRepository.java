package oth.ics.wtp.relaybackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import oth.ics.wtp.relaybackend.entities.Like;


@Repository
public interface LikeRepository extends JpaRepository<Like, Like.LikeId> {
    boolean existsByUserUsernameAndPostId(String username, Long postId);
}