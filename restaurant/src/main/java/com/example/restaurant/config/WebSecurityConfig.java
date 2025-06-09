package com.example.restaurant.config;

import com.example.restaurant.security.jwt.AuthEntryPointJwt;
import com.example.restaurant.security.jwt.AuthTokenFilter;
import com.example.restaurant.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Make sure this is imported if you use HttpMethod
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> {}) // Uses the global CORS config
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                // PUBLICLY ACCESSIBLE ENDPOINTS:
                                .requestMatchers("/api/auth/**").permitAll() // <<-- CRITICAL FOR REGISTER & LOGIN
                                .requestMatchers(HttpMethod.GET, "/api/menu/**").permitAll() // Public can view menu
                                // .requestMatchers("/public-endpoint/**").permitAll() // Add any other public endpoints

                                // AUTHENTICATED ENDPOINTS:
                                .requestMatchers("/api/orders/**").authenticated() // Example: orders require any authenticated user
                                // .requestMatchers("/api/users/me").authenticated() // Example for user profile

                                // ADMIN-ONLY ENDPOINTS (Example):
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                // More granular control for menu item modification:
                                // .requestMatchers(HttpMethod.POST, "/api/menu").hasRole("ADMIN")
                                // .requestMatchers(HttpMethod.PUT, "/api/menu/**").hasRole("ADMIN")
                                // .requestMatchers(HttpMethod.DELETE, "/api/menu/**").hasRole("ADMIN")

                                // DEFAULT: All other requests must be authenticated
                                .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}