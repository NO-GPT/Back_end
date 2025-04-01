package com.example.new_portfolio_server.config;

import com.example.new_portfolio_server.common.Response.JWTAuthResponse;
import com.example.new_portfolio_server.config.redis.RedisServiceImpl;
import com.example.new_portfolio_server.user.MyUserDetailsService;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

// JWT를 생성하고 검증하는 컴포넌트이고, JWT 생성과 유효성 검사 등의 로직을 포함하고 있다. 토큰과 관련된 모든 것은 여기서 이루어진다.
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // 토큰의 암호화/복호화를 위한 secret key
    @Value("${jwt.secret}")
    private String secretKey;

    public static final String BEARER = "Bearer";

    // Refresh Token 유효 기간 14일 (ms 단위)
    private final Long REFRESH_TOKEN_VALID_TIME = 14 * 1440 * 60 * 1000L;

    // Access Token 유효 기간 30분
    private final Long ACCESS_TOKEN_VALID_TIME = 30 * 60 * 1000L;

    private final MyUserDetailsService userDetailsService;

    private final RedisServiceImpl redisServiceImpl;

    // 의존성 주입이 완료된 후에 실행되는 메소드, secretKey를 Base64로 인코딩
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // JWT 토큰 생성
    public JWTAuthResponse generateToken(String username, Authentication authentication, Long userId) {
        String id = authentication.getName();

        Claims claims = Jwts.claims().setSubject(username); //사용자 아이디
        claims.put("userId", userId); //사용자 UID
        claims.put("name", username); //사용자 이름

        Date currentDate = new Date();
        Date accessTokenExpireDate = new Date(currentDate.getTime() + ACCESS_TOKEN_VALID_TIME);
        Date refreshTokenExpireDate = new Date(currentDate.getTime() + REFRESH_TOKEN_VALID_TIME);

        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(currentDate)
                .setExpiration(accessTokenExpireDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setExpiration(refreshTokenExpireDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        redisServiceImpl.setValues(username, refreshToken, Duration.ofMillis(REFRESH_TOKEN_VALID_TIME));

        return JWTAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(BEARER)
                .accessTokenExpireDate(ACCESS_TOKEN_VALID_TIME)
                .build();
    }

    // 리프레시 토큰 만료 시간을 가져오는 메서드
    public Long getRefreshTokenExpirationMillis() {
        return REFRESH_TOKEN_VALID_TIME;
    }

    // Access Token 의 만료 시간을 가져오는 메서드
    public Long getAccessTokenExpiration(String accessToken) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(accessToken)
                .getBody();
        Date expiration = claims.getExpiration();

        if (expiration != null) {
            return expiration.getTime();
        } else {
            // 만료 시간이 null이면 기본값인 0 반환
            return 0L;
        }
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 사용자 인증 정보 조회
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", new ArrayList<>());
    }

    // Request의 Header로부터 토큰 값 조회
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }

    // Request Header에 Refresh Token 정보를 추출하는 메서드
    public String resolveRefreshToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Refresh");
        if (StringUtils.hasText(bearerToken)) {
            return bearerToken;
        }
        return null;
    }

    // 토큰의 유효성 검증
    public boolean validateToken(String jwtToken) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
            return true;
        } catch (SecurityException e) {
            throw new JwtException("잘못된 JWT 서명입니다.");
        } catch (MalformedJwtException e) {
            throw new JwtException("잘못된 JWT 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw new JwtException("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new JwtException("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new JwtException("JWT 토큰의 구조가 유효하지 않습니다.");
        }
    }
}
