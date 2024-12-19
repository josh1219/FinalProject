package com.DogProject.config;

import com.DogProject.config.auth.OAuth2SuccessHandler;
import com.DogProject.service.CustomOAuth2UserService;
import com.DogProject.service.MemberService;
import com.DogProject.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final MemberService memberService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowSemicolon(true);
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowBackSlash(true);
        firewall.setAllowUrlEncodedPeriod(true);
        firewall.setAllowedHeaderNames(header -> true);  // 모든 헤더 이름 허용
        firewall.setAllowedHeaderValues(header -> true); // 모든 헤더 값 허용
        return firewall;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.httpFirewall(allowUrlEncodedSlashHttpFirewall())
           .ignoring()
           .antMatchers("/ws/**", "/topic/**", "/queue/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.securityContext((securityContext) -> securityContext
            .requireExplicitSave(true)
        );
        
        http
            .csrf()
                .ignoringAntMatchers("/member/checkEmail", "/api/**", "/dog/insert", "/dog/update/**", 
                    "/uploads/**", "/schedule/save", "/schedule/update")
                .and()
            .sessionManagement()
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/member/login?expired")
                .and()
                .sessionFixation().migrateSession()
                .invalidSessionUrl("/member/login?invalid")
                .and()
            .headers()
                .frameOptions().sameOrigin()
                .and()
            .authorizeRequests()
                .antMatchers("/", "/css/**", "/images/**", "/js/**", "/video/**","/member/**", "/error", 
                        "/home", "/board", "/board/detail", "/dog/**", "/shop/**", "/chat/**", "/walk/**", "/api/**",
                        "/uploads/**", "/schedule/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/member/checkEmail").permitAll()
                .antMatchers("/board/create").authenticated()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/member/login")
                .loginProcessingUrl("/member/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/member/login/error")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    String userEmail = authentication.getName();
                    String role = authentication.getAuthorities().toString();
                    
                    Member member = memberService.findBymEmail(userEmail);
                    String provider = member != null ? member.getProvider() : "unknown";
                    
                    // 세션에 로그인 정보 저장 (mIdx를 Integer로 저장)
                    HttpSession session = request.getSession();
                    session.setAttribute("member", member);
                    session.setAttribute("isLoggedIn", true);
                    session.setAttribute("mIdx", member.getMIdx());
                    session.setAttribute("email", userEmail);
                    session.setAttribute("role", role);
                    
                    // 쿠키 정보 저장 (Base64 인코딩)
                    String userInfo = String.format("%d|%s|%s|%s", 
                        member.getMIdx(), userEmail, provider, role);
                    String encodedUserInfo = Base64.getEncoder().encodeToString(
                        userInfo.getBytes(StandardCharsets.UTF_8));
                    
                    Cookie emailCookie = new Cookie("USER_INFO", encodedUserInfo);
                    emailCookie.setPath("/");
                    emailCookie.setMaxAge(3600);
                    response.addCookie(emailCookie);
                    
                    // 디버그 로그 추가
                    System.out.println("Session mIdx: " + session.getAttribute("mIdx"));
                    System.out.println("Session email: " + session.getAttribute("email"));
                    System.out.println("Cookie userInfo: " + userInfo);
                    
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
    }
}
