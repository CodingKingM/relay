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
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.repositories.*;
import oth.ics.wtp.relaybackend.services.UserService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class RelayControllerTestBase {
    private static final AtomicInteger testRunCounter = new AtomicInteger(0);
    protected String USER1_USERNAME;
    protected String USER1_PASSWORD;
    protected String USER2_USERNAME;
    protected String USER2_PASSWORD;
    protected String USER3_USERNAME;
    protected String USER3_PASSWORD;

    private static final String SESSION_USER_NAME = "userName";

    @Autowired protected UserRepository userRepository;
    @Autowired protected PostRepository postRepository;
    @Autowired protected LikeRepository likeRepository;
    @Autowired protected FollowRepository followRepository;
    @Autowired protected UserController userController;

    private final Map<String, HttpSession> sessions = new HashMap<>();

    @BeforeEach
    public void beforeEach() {
        cleanDatabase();
        int runId = testRunCounter.incrementAndGet();
        USER1_USERNAME = "testuser1_" + runId;
        USER1_PASSWORD = "pass1234_" + runId;
        USER2_USERNAME = "testuser2_" + runId;
        USER2_PASSWORD = "pass2345_" + runId;
        USER3_USERNAME = "testuser3_" + runId;
        USER3_PASSWORD = "pass3456_" + runId;
        createUser(USER1_USERNAME, USER1_PASSWORD);
        createUser(USER2_USERNAME, USER2_PASSWORD);
        createUser(USER3_USERNAME, USER3_PASSWORD);
        sessions.clear();
    }

    private void cleanDatabase() {
        likeRepository.deleteAll();
        followRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void createUser(String username, String password) {
        if (userRepository.existsById(username)) {
            return;
        }

        String authHeader = basic(username, password);
        userController.register(authHeader);
    }

    protected MockHttpServletRequest mockRequest(String username, String password) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (!sessions.containsKey(username)) {
            request.addHeader(HttpHeaders.AUTHORIZATION, basic(username, password));
            HttpSession session = request.getSession();
            sessions.put(username, session);
            session.setAttribute(SESSION_USER_NAME, username);
        } else {
            HttpSession session = sessions.get(username);
            request.setSession(session);
            session.setAttribute(SESSION_USER_NAME, username);
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