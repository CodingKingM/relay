package oth.ics.wtp.relaybackend.services;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import oth.ics.wtp.relaybackend.WeakCrypto;
import oth.ics.wtp.relaybackend.entities.User;
import oth.ics.wtp.relaybackend.repositories.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthServiceTest {
    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        User user = new User("authtest", WeakCrypto.hashPassword("testpass"));
        userRepository.save(user);
    }

    @Test
    public void testSuccessfulLogin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                WeakCrypto.base64encode("authtest:testpass"));

        User loggedInUser = authService.logIn(request);
        assertEquals("authtest", loggedInUser.getUsername());

        HttpSession session = request.getSession();
        assertEquals("authtest", session.getAttribute("userName"));
    }

    @Test
    public void testLoginWithWrongPassword() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                WeakCrypto.base64encode("authtest:wrongpass"));

        assertThrows(ResponseStatusException.class, () -> authService.logIn(request));
    }

    @Test
    public void testLoginWithNonExistentUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                WeakCrypto.base64encode("nouser:pass"));

        assertThrows(ResponseStatusException.class, () -> authService.logIn(request));
    }

    @Test
    public void testLoginWithoutAuthHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class, () -> authService.logIn(request));
    }

    @Test
    public void testLogout() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                WeakCrypto.base64encode("authtest:testpass"));

        authService.logIn(request);
        authService.logOut(request);

        assertNull(request.getSession(false));
    }

    @Test
    public void testGetAuthenticatedUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                WeakCrypto.base64encode("authtest:testpass"));

        authService.logIn(request);
        User user = authService.getAuthenticatedUser(request);
        assertEquals("authtest", user.getUsername());
    }

    @Test
    public void testGetAuthenticatedUserNotLoggedIn() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class,
                () -> authService.getAuthenticatedUser(request));
    }

    @Test
    public void testIsAuthenticated() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertFalse(authService.isAuthenticated(request));

        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                WeakCrypto.base64encode("authtest:testpass"));
        authService.logIn(request);

        assertTrue(authService.isAuthenticated(request));
    }

    @Test
    public void testRequireUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                WeakCrypto.base64encode("authtest:testpass"));
        authService.logIn(request);

        assertDoesNotThrow(() -> authService.requireUser(request, "authtest"));
        assertThrows(ResponseStatusException.class,
                () -> authService.requireUser(request, "otheruser"));
    }

    @Test
    public void testParseBasicAuth() {
        String authHeader = "Basic " + WeakCrypto.base64encode("user:pass");
        String[] parsed = authService.parseBasicAuth(authHeader);

        assertNotNull(parsed);
        assertEquals(2, parsed.length);
        assertEquals("user", parsed[0]);
        assertEquals("pass", parsed[1]);
    }

    @Test
    public void testParseBasicAuthInvalid() {
        assertNull(authService.parseBasicAuth(null));
        assertNull(authService.parseBasicAuth("InvalidHeader"));
        assertNull(authService.parseBasicAuth("Basic invalid_base64"));
    }
}