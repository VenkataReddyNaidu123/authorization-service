package Logistics.App.service;

import Logistics.App.entity.User;
import Logistics.App.exception.UserAlreadyExistsException;
import Logistics.App.repository.AuthorizationRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RegistrationService {

    private static final Logger logger= LoggerFactory.getLogger(RegistrationService.class);

    @Autowired
    private AuthorizationRepo authorizationRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(User user){
        String username = user.getUserName().trim();
        String emailNorm = user.getEmail().trim().toLowerCase(Locale.ROOT);

        logger.info("Attempting to register user username={} email={}", username, emailNorm);
        if (authorizationRepo.existsByUserName(username)) {
            logger.warn("Registration failed: username already exists username={}", username);
            throw new UserAlreadyExistsException("Username already exists: " + user.getUserName());
        }
        if (authorizationRepo.existsByEmail(emailNorm)) {
            logger.warn("Registration failed: email already exists email={}", emailNorm);
            throw new UserAlreadyExistsException("Username already exists: " + user.getEmail());
        }
        user.setRole("USER");
        String rawPassword = user.getPassword();
        String encoded = passwordEncoder.encode(rawPassword);
        user.setPassword(encoded);

        User saved = authorizationRepo.save(user);
        logger.info("User registered userId={} username={}", saved.getId(), saved.getUserName());
        return saved;

    }


    public User saveAdmin(User user){
        String username = user.getUserName().trim();
        String emailNorm = user.getEmail().trim().toLowerCase(Locale.ROOT);

        if (authorizationRepo.existsByUserName(username)) {
            throw new UserAlreadyExistsException("Username already exists: " + user.getUserName());
        }
        if (authorizationRepo.existsByEmail(emailNorm)) {
            throw new UserAlreadyExistsException("Username already exists: " + user.getEmail());
        }

        user.setUserName(username);
        user.setEmail(emailNorm);
        user.setRole("ADMIN");

        String encoded = passwordEncoder.encode(user.getPassword());
        user.setPassword(encoded);

        return authorizationRepo.save(user);
    }
}
