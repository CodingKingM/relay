package oth.ics.wtp.relaybackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import oth.ics.wtp.relaybackend.WeakCrypto;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.repositories.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class RelayControllerTestBase {
    protected static final String USER1_USERNAME = "testuser1";
    protected static final String USER1_PASSWORD = "pass1234";
    protected static final String USER2_USERNAME = "testuser2";
    protected static final String USER2_PASSWORD = "pass2345";
    protected static final String USER3_USERNAME = "testuser3";
    protected static final String USER3_PASSWORD = "pass3456";

    @Autowired protected UserRepository userRepository;

    private final Map<String, HttpSession> sessions = new HashMap<>();

    @BeforeEach
    public void beforeEach() {
        createUser(USER1_USERNAME, USER1_PASSWORD);
        createUser(USER2_USERNAME, USER2_PASSWORD);
        createUser(USER3_USERNAME, USER3_PASSWORD);
        sessions.clear();
    }

    private void createUser(String username, String password) {
        String passwordHash = WeakCrypto.hashPassword(password);
        User user = new User(username, passwordHash);
        userRepository.save(user);
    }

    protected MockHttpServletRequest mockRequest(String username, String password) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (!sessions.containsKey(username)) {
            request.addHeader(HttpHeaders.AUTHORIZATION, basic(username, password));
            sessions.put(username, request.getSession());
        } else {
            request.setSession(sessions.get(username));
        }
        return request;
    }

    protected HttpServletRequest user1() {
        return mockRequest(USER1_USERNAME, USER1_PASSWORD);
    }

    protected HttpServletRequest user2() {
        return mockRequest(USER2_USERNAME, USER2_PASSWORD);
    }

    protected HttpServletRequest user3() {
        return mockRequest(USER3_USERNAME, USER3_PASSWORD);
    }

    protected String basic(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}