package oth.ics.wtp.relaybackend.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.entities.Follow;
import oth.ics.wtp.relaybackend.repositories.UserRepository;
import oth.ics.wtp.relaybackend.repositories.FollowRepository;

@Service
public class FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public FollowService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    @Transactional
    public void followUser(String followerUsername, String followedUsername) {
        if (followerUsername.equals(followedUsername)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
        }

        User follower = userRepository.findById(followerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Follower not found"));

        User followed = userRepository.findById(followedUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User to follow not found"));

        if (followRepository.existsByFollowerUsernameAndFollowedUsername(followerUsername, followedUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already following this user");
        }

        Follow follow = new Follow(follower, followed);
        followRepository.save(follow);
    }

    @Transactional
    public void unfollowUser(String followerUsername, String followedUsername) {
        User follower = userRepository.findById(followerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Follower not found"));

        User followed = userRepository.findById(followedUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User to unfollow not found"));

        if (!followRepository.existsByFollowerUsernameAndFollowedUsername(followerUsername, followedUsername)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not following this user");
        }

        Follow.FollowId followId = new Follow.FollowId(followerUsername, followedUsername);
        followRepository.deleteById(followId);
    }

    public boolean isFollowing(String followerUsername, String followedUsername) {
        User follower = userRepository.findById(followerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Follower not found"));

        User followed = userRepository.findById(followedUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return followRepository.existsByFollowerUsernameAndFollowedUsername(followerUsername, followedUsername);
    }
}