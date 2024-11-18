package com.sodasensaitions.backend.config;

import com.sodasensaitions.backend.config.constants.HttpServletSessionConstants;
import com.sodasensaitions.backend.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;
  private final LogoutHandler logoutHandler;


  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests((authz) -> authz
            .requestMatchers(HttpServletSessionConstants.AUTHENTICATION_PATH + "/*").permitAll()
            .requestMatchers("/test/public").permitAll()
            .requestMatchers("/test/unreachable").authenticated()
            .requestMatchers("/payments/**").permitAll()
            .anyRequest().authenticated() // all other requests require authentication
        ).cors(AbstractHttpConfigurer::disable);
    http
        .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .logout((httpSecurityLogoutConfigurer -> {
          httpSecurityLogoutConfigurer.addLogoutHandler(logoutHandler);
          httpSecurityLogoutConfigurer.logoutUrl(HttpServletSessionConstants.AUTHENTICATION_PATH + "/logout");
          httpSecurityLogoutConfigurer.logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext());
        }));
    return http.build();
  }
}
