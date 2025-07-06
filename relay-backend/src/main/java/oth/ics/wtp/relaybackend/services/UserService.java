package oth.ics.wtp.relaybackend.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.WeakCrypto;
import oth.ics.wtp.relaybackend.dtos.CreateUserDto;
import oth.ics.wtp.relaybackend.dtos.UserDto;
import oth.ics.wtp.relaybackend.dtos.UserSearchDto;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.repositories.UserRepository;
import oth.ics.wtp.relaybackend.repositories.FollowRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public UserService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    public UserDto createUser(CreateUserDto createUserDto) {
        if (userRepository.existsByUsername(createUserDto.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        String hashedPassword = WeakCrypto.hashPassword(createUserDto.password());
        User user = new User(createUserDto.username(), hashedPassword);

        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toDto(user);
    }

    public List<UserSearchDto> searchUsers(String query, String currentUsername) {
        String pattern = "%" + query + "%";
        List<User> users = userRepository.searchByUsernamePattern(pattern);

        return users.stream()
                .map(user -> toSearchDto(user, currentUsername))
                .collect(Collectors.toList());
    }

    public void deleteUser(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(username);
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public UserDto updateUserProfile(String username, String fullName, String email, String biography) {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setBiography(biography);
        userRepository.save(user);
        return toDto(user);
    }

    private UserDto toDto(User user) {
        int followerCount = (int) followRepository.countByFollowedUsername(user.getUsername());
        int followingCount = (int) followRepository.countByFollowerUsername(user.getUsername());
        
        return new UserDto(
                user.getUsername(),
                user.getRegisteredAt().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                followerCount,
                followingCount,
                user.getFullName(),
                user.getEmail(),
                user.getBiography()
        );
    }

    private UserSearchDto toSearchDto(User user, String currentUsername) {
        boolean isFollowing = false;
        if (currentUsername != null) {
            isFollowing = followRepository.existsByFollowerUsernameAndFollowedUsername(currentUsername, user.getUsername());
        }

        return new UserSearchDto(
                user.getUsername(),
                user.getRegisteredAt(),
                isFollowing
        );
    }

    public List<UserSearchDto> getFollowers(String username) {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return followRepository.findFollowersByUsername(username).stream()
                .map(follower -> toSearchDto(follower, username))
                .collect(Collectors.toList());
    }

    public List<UserSearchDto> getFollowing(String username) {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return followRepository.findFollowingByUsername(username).stream()
                .map(following -> toSearchDto(following, username))
                .collect(Collectors.toList());
    }
}