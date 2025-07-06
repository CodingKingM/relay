package oth.ics.wtp.relaybackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import oth.ics.wtp.relaybackend.entities.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, Like.LikeId> {
    boolean existsByUserAndPost(String user, Long post);
    int countByPost(Long post);
    @Modifying
    @Query("DELETE FROM Like l WHERE l.user = :user AND l.post = :post")
    void deleteByUserAndPost(@Param("user") String user, @Param("post") Long post);
    
    @Modifying
    @Query("DELETE FROM Like l WHERE l.post = :postId")
    void deleteByPost(@Param("postId") Long postId);
}