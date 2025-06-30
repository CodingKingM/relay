package oth.ics.wtp.relaybackend.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.WeakCrypto;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.repositories.UserRepository;

import java.util.Optional;

@Service
public class AuthService {

    private static final String SESSION_USER_NAME = "userName";
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User logIn(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                throw new Exception("Missing or invalid Authorization header");
            }

            String decoded = WeakCrypto.base64decode(authHeader.substring("Basic ".length()));
            String[] parts = decoded.split(":", 2);

            if (parts.length != 2) {
                throw new Exception("Invalid credentials format");
            }

            String userName = parts[0];
            String password = parts[1];

            String hashedPassword = WeakCrypto.hashPassword(password);

            User user = userRepository.findById(userName)
                    .orElseThrow(() -> new Exception("User not found"));

            if (!user.getHashedPassword().equals(hashedPassword)) {
                throw new Exception("Invalid password");
            }

            request.getSession().setAttribute(SESSION_USER_NAME, userName);

            return user;

        } catch (Exception e) {
            logOut(request);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
    }

    public void logOut(HttpServletRequest request) {
        request.getSession().removeAttribute(SESSION_USER_NAME);
        request.getSession().invalidate();
    }

    public User getAuthenticatedUser(HttpServletRequest request) {
        String userName = (String) request.getSession().getAttribute(SESSION_USER_NAME);

        if (userName == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        return userRepository.findById(userName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public boolean isAuthenticated(HttpServletRequest request) {
        String userName = (String) request.getSession().getAttribute(SESSION_USER_NAME);
        return userName != null && userRepository.existsByUsername(userName);
    }

    public Optional<String> getAuthenticatedUsername(HttpServletRequest request) {
        String userName = (String) request.getSession().getAttribute(SESSION_USER_NAME);
        return Optional.ofNullable(userName);
    }

    public void requireUser(HttpServletRequest request, String username) {
        User currentUser = getAuthenticatedUser(request);
        if (!currentUser.getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only access your own resources");
        }
    }

    public String[] parseBasicAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }

        try {
            String decoded = WeakCrypto.base64decode(authHeader.substring("Basic ".length()));
            String[] parts = decoded.split(":", 2);
            return parts.length == 2 ? parts : null;
        } catch (Exception e) {
            return null;
        }
    }
}