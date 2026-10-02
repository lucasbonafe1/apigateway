package example.redis.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desativamos CSRF porque é uma API stateless (sem sessão/cookie de navegador).
                // CSRF protege contra ataques que abusam de sessões autenticadas via cookie -
                // não se aplica aqui, já que autenticação futura será via token (JWT).
                .csrf(csrf -> csrf.disable())

                // Sem sessão HTTP - cada requisição deve se autenticar sozinha (via token,
                // no futuro). Isso é o padrão para APIs REST.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Libera login e register para qualquer um, sem autenticação
                        .requestMatchers("/api/auth/**").permitAll()
                        // Qualquer outra rota exige autenticação
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
