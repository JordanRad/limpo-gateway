package limpo.gateway.config;

import limpo.gateway.filter.AuthFilter;
import limpo.gateway.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class GatewayConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AuthFilter filter;


    @Autowired
    private JwtUserDetailsService userDetailService;


    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable().cors().and()
                .authorizeRequests()
                // Blacklisted routes
                .antMatchers("/order-service/api/v1/orders/*").access("hasRole('ADMIN')")
                .antMatchers(HttpMethod.POST, "/order-service/api/v1/limpoUnits/*").access("hasRole('ADMIN')")
                .antMatchers(HttpMethod.DELETE, "/order-service/api/v1/limpoUnits/*").access("hasRole('ADMIN')")
                .antMatchers(HttpMethod.GET, "/order-service/api/v1/clients/*").access("hasRole('ADMIN')")
                .antMatchers(HttpMethod.PUT, "/order-service/api/v1/clients/*").access("hasRole('ADMIN')")
                .antMatchers(HttpMethod.DELETE, "/order-service/api/v1/clients/*").access("hasRole('ADMIN')")

                // Whitelisted routes
                .antMatchers("/auth-service/api/v1/*").permitAll()
                .antMatchers(HttpMethod.POST, "/order-service/api/v1/clients").permitAll()
                .antMatchers(HttpMethod.GET, "/order-service/api/v1/limpoUnits").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .exceptionHandling()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    }

}
