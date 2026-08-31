package com.example.multimedia.file_upload_api.config;

import com.example.multimedia.file_upload_api.security.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // /api/users/register used to be here too — removed: it let an
                        // unauthenticated caller create an account in any tenant with any role
                        // (caller-supplied superAdminId/authKey, no ownership check). It's now
                        // admin-gated like the rest of user management (UserDetailService), so it
                        // falls under the generic "/api/users/**".authenticated() rule below.
                        .requestMatchers("/api/users/login", "/api/users/generate-hash/**", "/employee/login", "/api/employee/login").permitAll()
                        // Browser redirects, hit before anyone is authenticated — same reasoning
                        // as /api/users/login above. /api/users/session (the endpoint the SSO
                        // callback lands on afterwards) stays behind the existing
                        // "/api/users/**".authenticated() rule below, unchanged.
                        .requestMatchers("/api/auth/microsoft/**").permitAll()
                        .requestMatchers("/api/super-admin/**").permitAll()
                        .requestMatchers("/api/v1/bom/master/**").hasAuthority("super_admin")
                        .requestMatchers("/api/business-card/**").permitAll()
                        .requestMatchers("/api/ocr/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        // WorkFlow's outbound webhook — authenticated by its own HMAC signature
                        // check (SupplierRegistrationController), not JWT.
                        .requestMatchers("/api/webhooks/**").permitAll()
                        // Department list — public so Create Employee form can load the dropdown without auth
                        .requestMatchers("/api/departments/**").permitAll()
                        .requestMatchers("/api/company-addresses/**").authenticated()
                        .requestMatchers("/api/upload/**").authenticated()
                        .requestMatchers("/api/files/**").permitAll()
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/material/upload", "/api/material/all").permitAll()
                        .requestMatchers("/api/vendor-master/upload", "/api/vendor-master/all").permitAll()
                        .requestMatchers("/api/material-master/upload").permitAll()
                        .requestMatchers("/api/master-purchase-orders/upload", "/api/master-purchase-orders/sync-folderit").permitAll()
                        .requestMatchers("/api/purchase-orders/from-awarded-quotation/**").permitAll()
                        .requestMatchers("/api/vendor/**").permitAll()
                        .requestMatchers("/api/vendors/**").permitAll()
                        .requestMatchers("/api/employee/gate-entry/**").permitAll()
                        .requestMatchers("/api/employee/material-inward/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}