package Logistics.App.dto;

import jakarta.validation.constraints.NotNull;

public class RefreshRequest {
    @NotNull
    private String refreshToken;

    public RefreshRequest() { }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
