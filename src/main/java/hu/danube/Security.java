package hu.danube;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class Security {
  @Bean
  SecurityFilterChain chain(HttpSecurity h) throws Exception {
    return h.authorizeHttpRequests(a -> a.anyRequest().permitAll()).build();
  }
}
