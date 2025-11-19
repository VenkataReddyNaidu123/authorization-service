package Logistics.App.dto;

import jakarta.validation.constraints.NotNull;

public class LoginRequest {

    @NotNull(message = "Username or Email is required")
    private String userNameOrEmail;

    @NotNull(message = "Password is required")
    private String password;

    public String getUserNameOrEmail() { return userNameOrEmail; }
    public void setUserNameOrEmail(String userNameOrEmail) { this.userNameOrEmail = userNameOrEmail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
