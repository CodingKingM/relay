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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    private UserDto toDto(User user) {
        return new UserDto(
                user.getUsername(),
                user.getRegisteredAt(),
                user.getFollowers().size(),
                user.getFollowing().size()
        );
    }

    private UserSearchDto toSearchDto(User user, String currentUsername) {
        boolean isFollowing = false;
        if (currentUsername != null) {
            User currentUser = userRepository.findById(currentUsername).orElse(null);
            if (currentUser != null) {
                isFollowing = currentUser.getFollowing().contains(user);
            }
        }

        return new UserSearchDto(
                user.getUsername(),
                user.getRegisteredAt(),
                isFollowing
        );
    }
}