package Logistics.App.controller;

import Logistics.App.dto.RegistrationRequest;
import Logistics.App.entity.User;
import Logistics.App.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logistics/admin")
public class AdminController {

    private final RegistrationService registrationService;

    public AdminController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody RegistrationRequest req){
        try {
            User user = new User();
            user.setUserName(req.getUserName());
            user.setEmail(req.getEmail());
            user.setPassword(req.getPassword());
            User saved = registrationService.saveAdmin(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("Admin created: " + saved.getUserName());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }
}
