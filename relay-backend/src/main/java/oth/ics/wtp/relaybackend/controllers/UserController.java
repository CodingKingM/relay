package oth.ics.wtp.relaybackend.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import oth.ics.wtp.relaybackend.dtos.CreateUserDto;
import oth.ics.wtp.relaybackend.dtos.UserDto;
import oth.ics.wtp.relaybackend.dtos.UserSearchDto;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.services.AuthService;
import oth.ics.wtp.relaybackend.services.FollowService;
import oth.ics.wtp.relaybackend.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final FollowService followService;

    public UserController(UserService userService, AuthService authService, FollowService followService) {
        this.userService = userService;
        this.authService = authService;
        this.followService = followService;
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String[] credentials = authService.parseBasicAuth(authHeader);
        if (credentials == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid authorization header");
        }

        CreateUserDto createUserDto = new CreateUserDto(credentials[0], credentials[1]);
        return userService.createUser(createUserDto);
    }

    @SecurityRequirement(name = "basicAuth")
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto login(HttpServletRequest request) {
        User user = authService.logIn(request);
        return userService.getUserByUsername(user.getUsername());
    }

    @SecurityRequirement(name = "basicAuth")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        authService.logOut(request);
    }

    @SecurityRequirement(name = "basicAuth")
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserSearchDto> searchUsers(
            @RequestParam String q,
            HttpServletRequest request) {
        String currentUsername = authService.getAuthenticatedUsername(request).orElse(null);
        return userService.searchUsers(q, currentUsername);
    }

    @SecurityRequirement(name = "basicAuth")
    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto getUser(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @SecurityRequirement(name = "basicAuth")
    @PostMapping("/{username}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void followUser(
            @PathVariable String username,
            HttpServletRequest request) {
        User currentUser = authService.getAuthenticatedUser(request);
        followService.followUser(currentUser.getUsername(), username);
    }

    @SecurityRequirement(name = "basicAuth")
    @DeleteMapping("/{username}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollowUser(
            @PathVariable String username,
            HttpServletRequest request) {
        User currentUser = authService.getAuthenticatedUser(request);
        followService.unfollowUser(currentUser.getUsername(), username);
    }

    @SecurityRequirement(name = "basicAuth")
    @GetMapping("/me")
    public UserDto getCurrentUserProfile(HttpServletRequest request) {
        User user = authService.getAuthenticatedUser(request);
        return userService.getUserByUsername(user.getUsername());
    }

    @SecurityRequirement(name = "basicAuth")
    @PutMapping("/me")
    public UserDto updateCurrentUserProfile(@RequestBody UserDto userDto, HttpServletRequest request) {
        User user = authService.getAuthenticatedUser(request);
        return userService.updateUserProfile(
            user.getUsername(),
            userDto.fullName(),
            userDto.email(),
            userDto.biography()
        );
    }
}