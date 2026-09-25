package bibliotheque.config;

import bibliotheque.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .userDetailsService(userDetailsService)
            
            .headers(headers -> headers
            .httpStrictTransportSecurity(hsts -> hsts
            .includeSubDomains(true)
            .maxAgeInSeconds(31536000)
            )
            .frameOptions(frame -> frame.sameOrigin())
            )
            .authorizeHttpRequests(auth -> auth
                // ===== PUBLIC =====
                .requestMatchers("/login", "/403", "/css/**", "/js/**", "/images/**", "/favicon.ico")
                    .permitAll()
                .requestMatchers("/mentions-legales", "/cgu", "/confidentialite")
                    .permitAll()

                // ===== ADMIN UNIQUEMENT =====
                .requestMatchers("/utilisateurs/**").hasRole("BIB")
                .requestMatchers("/journal").hasRole("BIB")
                .requestMatchers("/dashboard").hasRole("BIB")
                .requestMatchers("/livres/nouveau", "/livres/*/modifier", "/livres/*/supprimer")
                    .hasRole("BIB")
                .requestMatchers("/emprunts/nouveau").hasRole("BIB")

                // ===== CONNECTÉ =====
                .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex
                .accessDeniedPage("/403")
            )

            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}