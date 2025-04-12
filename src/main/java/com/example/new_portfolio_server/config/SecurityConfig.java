package com.example.new_portfolio_server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity          // http 보안 요청
@EnableMethodSecurity       // 메서드 보안 요청
//@EnableWebFluxSecurity    // WebFlux 기반 시큐리티 활성화
@RequiredArgsConstructor
public class SecurityConfig {
//    private final UserDetailsService userDetailsService;                  // 스프링 시쿠리티6 부턴 자동으로 주입 ??

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorizeRequests -> authorizeRequests
                        .requestMatchers("/users").permitAll()              // 인증없이 접속 가능
                        .requestMatchers("/users/signup").permitAll()       // 회원가입도
                        .requestMatchers("/auth/login").permitAll()         // 로그인도 마찬가지
                        .anyRequest().authenticated()                         // 나머지 경로는 모두 인증 필요
                )
//                .userDetailsService(userDetailsService)                     // 시큐리티 6부턴 자동으로 감지 한다던데??
                .httpBasic(Customizer.withDefaults());                        // http 기본 인증??
        return http.build();
    }
}
