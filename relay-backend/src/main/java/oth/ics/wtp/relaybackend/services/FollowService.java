package oth.ics.wtp.relaybackend.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.repositories.UserRepository;

@Service
public class FollowService {

    private final UserRepository userRepository;

    public FollowService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        if (follower.getFollowing().contains(followed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already following this user");
        }

        follower.getFollowing().add(followed);
        followed.getFollowers().add(follower);

        userRepository.save(follower);
        userRepository.save(followed);
    }

    @Transactional
    public void unfollowUser(String followerUsername, String followedUsername) {
        User follower = userRepository.findById(followerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Follower not found"));

        User followed = userRepository.findById(followedUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User to unfollow not found"));

        if (!follower.getFollowing().contains(followed)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not following this user");
        }

        follower.getFollowing().remove(followed);
        followed.getFollowers().remove(follower);

        userRepository.save(follower);
        userRepository.save(followed);
    }

    public boolean isFollowing(String followerUsername, String followedUsername) {
        User follower = userRepository.findById(followerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Follower not found"));

        User followed = userRepository.findById(followedUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return follower.getFollowing().contains(followed);
    }
}