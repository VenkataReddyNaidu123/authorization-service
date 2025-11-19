package Logistics.App.config;

import Logistics.App.jwtConfig.JwtAuthenticationFilter;
import Logistics.App.jwtConfig.JwtUtil;
import Logistics.App.repository.AuthorizationRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthorizationRepo authorizationRepo;
    private final JwtUtil jwtUtil;

    public SecurityConfig(AuthorizationRepo authorizationRepo, JwtUtil jwtUtil) {
        this.authorizationRepo = authorizationRepo;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> authorizationRepo.findByUserName(username)
                .map(user -> {
                    String role = user.getRole() == null ? "USER" : user.getRole();
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    return new User(user.getUserName(), user.getPassword(), List.of(authority));
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        var jwtFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/logistics/register", "/logistics/login").permitAll()
                        .requestMatchers("/logistics/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
