package oth.ics.wtp.relaybackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import oth.ics.wtp.relaybackend.entities.Follow;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Follow.FollowId> {
}