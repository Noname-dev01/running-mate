package portfolio2023.runningmate.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import portfolio2023.runningmate.service.AccountService;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final DataSource dataSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .mvcMatchers("/","/running-mate","/running-mate/sign-up","/running-mate/login"
                        ,"/resources/**","/running-mate/check-email-token","/running-mate/email-login",
                        "/running-mate/login-by-email","/running-mate/check-login-email","/running-mate/login-link"
                        ,"/running-mate/search/crew","/profile").permitAll()
                .mvcMatchers(HttpMethod.GET, "/running-mate/profile/*").permitAll()
                .anyRequest().authenticated()
            .and()
                .formLogin()
                .loginPage("/running-mate/login")
                .defaultSuccessUrl("/running-mate")
                .permitAll()
            .and()
                .rememberMe()
                .userDetailsService(userDetailsService)
                .tokenRepository(tokenRepository())
            .and()
                .logout()
                .logoutSuccessUrl("/running-mate")
                .logoutUrl("/running-mate/logout");


        return http.build();
    }

    @Bean
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().mvcMatchers("/node_modules/**","/resources/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
