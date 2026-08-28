package mca.fincorebanking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import mca.fincorebanking.security.CustomAuthenticationFailureHandler;
import mca.fincorebanking.security.CustomAuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
            CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/login", "/register", "/css/**", "/js/**",
                                "/images/**", "/error","/reports/statement")
                        .permitAll()

                        .requestMatchers("/dashboard").authenticated()

                        .requestMatchers("/accounts/**", "/transactions/**", "/investments/**",
                                "/kyc/**","/reports/statement/pdf","/reports/statement/csv")
                        .hasAnyRole("CUSTOMER", "CORPORATE")

                        .requestMatchers("/teller/**").hasRole("TELLER")
                        

                        .requestMatchers("/manager/**").hasRole("MANAGER")

                        .requestMatchers("/compliance/actions/**").hasRole("COMPLIANCE")

                        .requestMatchers("/compliance/audit/**", "/compliance/reports/**",
                                "/reports/**")
                        .hasAnyRole("COMPLIANCE", "AUDITOR")

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers("/super-admin/**").hasRole("SUPER_ADMIN").anyRequest()
                        .authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }
}