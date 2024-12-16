package com.DogProject.config;

import com.DogProject.config.auth.OAuth2SuccessHandler;
import com.DogProject.service.CustomOAuth2UserService;
import com.DogProject.service.MemberService;
import com.DogProject.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final MemberService memberService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf()
                .ignoringAntMatchers("/member/checkEmail", "/api/**", "/dog/insert", "/dog/update/**", "/ws/**", "/ws/chat/**")  // WebSocket 경로 CSRF 검사 제외
                .and()
            .sessionManagement()
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)  // 동시 로그인 허용으로 변경
                .expiredUrl("/member/login?expired")
                .and()
                .sessionFixation().migrateSession()  // 세션 마이그레이션으로 변경
                .invalidSessionUrl("/member/login?invalid")
                .and()
            .headers()
                .frameOptions().sameOrigin()
                .and()
            .authorizeRequests()
                .antMatchers("/", "/css/**", "/images/**", "/js/**", "/video/**","/member/**", "/error", 
                        "/home", "/board/**", "/dog/**", "/shop/**", "/chat/**", "/walk/**", "/api/**",
                        "/ws/**", "/ws/chat/**", "/topic/**", "/schedule/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")  
                .antMatchers("/member/checkEmail").permitAll()  // 이메일 중복 체크 API 허용
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/member/login")
                .loginProcessingUrl("/member/login")
                .defaultSuccessUrl("/home", true)  // true를 추가하여 항상 홈으로 리다이렉트
                .failureUrl("/member/login/error")  // 실패 URL 수정
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    String userEmail = authentication.getName();
                    String role = authentication.getAuthorities().toString();
                    
                    // MemberService를 통해 사용자 정보 조회
                    Member member = memberService.findBymEmail(userEmail);
                    String provider = member != null ? member.getProvider() : "unknown";
                    
                    // 세션에 로그인 정보 저장
                    HttpSession session = request.getSession();
                    session.setAttribute("member", member);
                    session.setAttribute("isLoggedIn", true);
                    session.setAttribute("mIdx", member.getMIdx());  
                    
                    // USER_INFO 쿠키에 이메일, 권한, provider, mIdx 정보 포함
                    var userInfo = member.getMIdx() + "★" + userEmail + "★" + provider + "★" + role;  // mIdx 추가
                    Cookie emailCookie = new Cookie("USER_INFO", userInfo);
                    emailCookie.setPath("/");
                    emailCookie.setMaxAge(3600); // 1시간
                    response.addCookie(emailCookie);
                    response.sendRedirect("/home");
                })
                .permitAll()
                .and()
            .logout()
                .logoutUrl("/member/logout")
                .logoutSuccessUrl("/member/login")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("USER_INFO", "remember-me")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");
                    response.sendRedirect("/member/login");
                })
                .permitAll()
            .and()
            .rememberMe()  
                .key("uniqueAndSecret")  
                .tokenValiditySeconds(60 * 60 * 24 * 30)  
                .rememberMeParameter("remember-me")  
                .userDetailsService(memberService)  
                .and()
                .oauth2Login()
                .loginPage("/member/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/member/login?error=true")
                .successHandler(oAuth2SuccessHandler)
                .userInfoEndpoint()
                    .userService(customOAuth2UserService)
                .and()
                .authorizationEndpoint()
                    .baseUri("/oauth2/authorization");

        return http.build();
    }
}
