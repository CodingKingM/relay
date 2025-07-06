package oth.ics.wtp.relaybackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import oth.ics.wtp.relaybackend.entities.Follow;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Follow.FollowId> {

    boolean existsByFollowerUsernameAndFollowedUsername(String followerUsername, String followedUsername);

    @Query("SELECT f.followed FROM Follow f WHERE f.follower.username = :username")
    List<Follow> findFollowingByUsername(@Param("username") String username);

    @Query("SELECT f.follower FROM Follow f WHERE f.followed.username = :username")
    List<Follow> findFollowersByUsername(@Param("username") String username);

    long countByFollowerUsername(String followerUsername);

    long countByFollowedUsername(String followedUsername);
}