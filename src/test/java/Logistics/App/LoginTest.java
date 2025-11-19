package Logistics.App;

import Logistics.App.controller.UserRegisterController;
import Logistics.App.dto.RefreshRequest;
import Logistics.App.dto.RegistrationRequest;
import Logistics.App.entity.RefreshToken;
import Logistics.App.entity.User;
import Logistics.App.exception.InvalidCredentialsException;
import Logistics.App.exception.UserNotFoundException;
import Logistics.App.jwtConfig.JwtUtil;
import Logistics.App.repository.AuthorizationRepo;
import Logistics.App.service.RegistrationService;
import Logistics.App.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRegisterController.class)
@AutoConfigureMockMvc(addFilters = false) // disable Spring Security filters for controller tests
class UserRegisterControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @MockitoBean
    private RegistrationService registrationService;
    @MockitoBean
    private AuthorizationRepo authorizationRepo;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private TokenService tokenService;


    @Test
    void register_success_returnsCreatedAndBody() throws Exception {
        // Valid strong password: at least 8 chars, contains uppercase, number and special char
        String rawJson = """
                {
                  "userName": "newuser",
                  "email": "newuser@example.com",
                  "password": "StrongP@ss1",
                  "organization": "org1"
                }
                """;

        User saved = new User();
        saved.setId(10L);
        saved.setUserName("newuser");
        saved.setEmail("newuser@example.com");
        saved.setRole("USER");

        when(registrationService.saveUser(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/logistics/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userName").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));

        verify(registrationService, times(1)).saveUser(any(User.class));
    }

    @Test
    void register_duplicate_organization_returnsConflict_withMessage() throws Exception {
        String rawJson = """
                {
                  "userName": "dup",
                  "email": "dup@example.com",
                  "password": "StrongP@ss1",
                  "organization": "org1"
                }
                """;

        when(registrationService.saveUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("username already exists"));

        mockMvc.perform(post("/logistics/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("username already exists")));

        verify(registrationService, times(1)).saveUser(any(User.class));
    }

    // ---------- LOGIN ----------
    @Test
    void login_byEmail_success_returnsTokenJson() throws Exception {
        String rawJson = "{ \"userNameOrEmail\": \"bob@example.com\", \"password\": \"secret\" }";

        User stored = new User();
        stored.setUserName("bob");
        stored.setEmail("bob@example.com");
        stored.setPassword("encodedPw");
        stored.setRole("USER");

        when(authorizationRepo.findByEmail("bob@example.com")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("secret", "encodedPw")).thenReturn(true);
        when(jwtUtil.generateToken("bob", "USER")).thenReturn("jwt.token.here");

        mockMvc.perform(post("/logistics/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authorizationRepo).findByEmail("bob@example.com");
        verify(passwordEncoder).matches("secret", "encodedPw");
        verify(jwtUtil).generateToken("bob", "USER");
    }

    @Test
    void login_byUsername_success_returnsTokenJson() throws Exception {
        String rawJson = "{ \"userNameOrEmail\": \"alice\", \"password\": \"p\" }";

        User stored = new User();
        stored.setUserName("alice");
        stored.setEmail("alice@example.com");
        stored.setPassword("enc");
        stored.setRole("USER");

        when(authorizationRepo.findByUserName("alice")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("p", "enc")).thenReturn(true);
        when(jwtUtil.generateToken("alice", "USER")).thenReturn("tokenX");

        mockMvc.perform(post("/logistics/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tokenX"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authorizationRepo).findByUserName("alice");
        verify(jwtUtil).generateToken("alice", "USER");
    }

    @Test
    void login_userNotFound_throwsUserNotFoundException() throws Exception {
        String rawJson = "{ \"userNameOrEmail\": \"missingUser\", \"password\": \"p\" }";

        when(authorizationRepo.findByUserName("missingUser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/logistics/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                // controller throws UserNotFoundException — assert a 4xx and that exception type is thrown
                .andExpect(status().is4xxClientError())
                .andExpect(result -> {
                    Throwable resolved = result.getResolvedException();
                    assert resolved != null;
                    if (!(resolved instanceof UserNotFoundException)) {
                        throw new AssertionError("Expected UserNotFoundException but was: " + resolved.getClass());
                    }
                });

        verify(authorizationRepo).findByUserName("missingUser");
    }

    @Test
    void login_storedPasswordMissing_returnsInternalServerErrorWithMessage() throws Exception {
        String rawJson = "{ \"userNameOrEmail\": \"jane\", \"password\": \"p\" }";

        User stored = new User();
        stored.setUserName("jane");
        stored.setPassword(null); // missing password

        when(authorizationRepo.findByUserName("jane")).thenReturn(Optional.of(stored));

        mockMvc.perform(post("/logistics/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Server error: stored password missing")));

        verify(authorizationRepo).findByUserName("jane");
    }

    @Test
    void login_incorrectPassword_throwsInvalidCredentialsException() throws Exception {
        String rawJson = "{ \"userNameOrEmail\": \"joe\", \"password\": \"wrong\" }";

        User stored = new User();
        stored.setUserName("joe");
        stored.setPassword("encpw");

        when(authorizationRepo.findByUserName("joe")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("wrong", "encpw")).thenReturn(false);

        mockMvc.perform(post("/logistics/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> {
                    Throwable resolved = result.getResolvedException();
                    assert resolved != null;
                    if (!(resolved instanceof InvalidCredentialsException)) {
                        throw new AssertionError("Expected InvalidCredentialsException but was: " + resolved.getClass());
                    }
                });

        verify(passwordEncoder).matches("wrong", "encpw");
    }


    @Test
    void logout_withExistingRefreshToken_revokesAndReturnsOk() throws Exception {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("refresh-123");

        RefreshToken tokenObj = new RefreshToken();
        tokenObj.setToken("refresh-123");

        when(tokenService.findByToken("refresh-123"))
                .thenReturn(Optional.of(tokenObj));

        mockMvc.perform(post("/logistics/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Logged out")));

        verify(tokenService).findByToken("refresh-123");
        verify(tokenService).revokeRefreshToken(any());
    }

    @Test
    void logout_withMissingRefreshToken_returnsOkAndDoesNotRevoke() throws Exception {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("no-such-token");

        when(tokenService.findByToken("no-such-token")).thenReturn(Optional.empty());

        mockMvc.perform(post("/logistics/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Logged out")));

        verify(tokenService).findByToken("no-such-token");
        verify(tokenService, never()).revokeRefreshToken(any());
    }
}
