package com.example.new_portfolio_server.common.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JWTAuthResponse {

    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpireDate;

    @Builder
    public JWTAuthResponse(String tokenType, String accessToken, String refreshToken, Long accessTokenExpireDate) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpireDate = accessTokenExpireDate;
    }
}
