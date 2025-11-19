package Logistics.App.dto;

import Logistics.App.passwordChecker.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegistrationRequest {

    @NotNull
    @Size(min = 3, max = 30, message = "Name must be between 3 and 30 chars")
    private String userName;

    @NotNull
    @Email(message = "Email is invalid")
    private String email;

    @StrongPassword
    private String password;

    public RegistrationRequest() {}


    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}