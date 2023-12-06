package com.cojar.whats_hot.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final ApiAuthenticationEntrypoint apiAuthenticationEntrypoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/**") // 아래의 모든 설정 /api/** 경로에만 적용
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers(HttpMethod.GET, "/api/index").permitAll() // get:/api/index 아무나 접속 가능
                        .requestMatchers(HttpMethod.POST, "/api/members/signup").permitAll() // post:/api/members/signup 아무나 접속 가능
                        .requestMatchers(HttpMethod.POST, "/api/members/login").permitAll() // post:/api/members/login 아무나 접속 가능
//                        .requestMatchers(HttpMethod.GET, "/api/events").permitAll() // get:/api/events 아무나 접속 가능
//                        .requestMatchers(HttpMethod.GET, "/api/events/*").permitAll() // get:/api/events/# 아무나 접속 가능
                        .anyRequest().authenticated()) // 그 외는 인증된 사용자만 접속 가능
                .cors(cors -> cors
                        .disable()) // 타 도메인에서 API 호출 가능
                .csrf(csrf -> csrf
                        .disable()) // CSRF 토큰 끄기
                .httpBasic(httpBasic -> httpBasic
                        .disable()) // httpBasic 로그인 방식 끄기
                .formLogin(formLogin -> formLogin
                        .disable()) // 폼 로그인 방식 끄기
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 끄기
                .addFilterBefore( // b filter 실행 전 a filter 실행
                        jwtAuthorizationFilter, // 강제 인증 할당 메서드 실행
                        UsernamePasswordAuthenticationFilter.class
                )
        ;

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
