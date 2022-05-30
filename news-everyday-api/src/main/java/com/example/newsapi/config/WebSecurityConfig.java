package com.example.newsapi.config;

import com.auth0.jwt.algorithms.Algorithm;
import com.example.newsapi.filter.CustomAuthorizationFilter;
import com.example.newsapi.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${secure.jwt.secret}")
    private final String secret;
    private final RestTemplate restTemplate;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers(HttpMethod.POST, "/user/auth").permitAll();
        http.authorizeRequests().antMatchers(HttpMethod.GET, "/user/test").hasAnyAuthority("USER");
        http.authorizeRequests().anyRequest().authenticated();

        http.csrf().disable();

        http.addFilterBefore(new CustomAuthorizationFilter(getTokenUtils(), restTemplate), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public TokenUtils getTokenUtils() {
        return new TokenUtils(getAlgorithm());
    }

    @Bean
    public Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret.getBytes());
    }
}
