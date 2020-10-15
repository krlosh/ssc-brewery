package guru.sfg.brewery.config;

import guru.sfg.brewery.security.JpaUserDetailsService;
import guru.sfg.brewery.security.RestHeaderAuthFilter;
import guru.sfg.brewery.security.RestUrlAuthFilter;
import guru.sfg.brewery.security.SfgPasswordEncoderFactories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapReferralException;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    public RestHeaderAuthFilter restHeaderAuthFilter(AuthenticationManager authenticationManager){
        RestHeaderAuthFilter filter = new RestHeaderAuthFilter(new AntPathRequestMatcher("/api/**"));
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    public RestUrlAuthFilter restUrlAuthFilter(AuthenticationManager authenticationManager){
        RestUrlAuthFilter filter = new RestUrlAuthFilter(new AntPathRequestMatcher("/api/**"));
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.addFilterBefore(restHeaderAuthFilter(authenticationManager()),
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(restUrlAuthFilter(authenticationManager()),
                    UsernamePasswordAuthenticationFilter.class)
        .csrf().disable();

        http
                .authorizeRequests(authorize -> {
                    authorize
                            .antMatchers("/h2-console/**").permitAll()
                            .antMatchers("/", "/webjars/**","/login","/resources/**").permitAll()
                            .antMatchers("/beers/find","/beers*").permitAll()
                            .antMatchers(HttpMethod.GET,"/api/v1/beer/**").permitAll()
                            .mvcMatchers(HttpMethod.GET,"/api/v1/beerUpc/{upc}").permitAll();
                })
                .authorizeRequests()
                    .anyRequest().authenticated()
                .and()
                    .formLogin()
                .and()
                    .httpBasic();
        http.headers().frameOptions().sameOrigin();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return SfgPasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /*
    Not needed if you only have one UserDetailsService impl, because spring boot uses that.

    @Autowired
    JpaUserDetailsService jpaUserDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(this.jpaUserDetailsService):
        /*auth.inMemoryAuthentication()
                .withUser("spring")
                .password("{bcrypt}$2a$10$3y9S1INTKqWlDKW1.aNJd.OrE9IfIooCA1z1xx1hbGa3mosa1kVce") //To setup password encoder use {}, noop is none
                .roles("ADMIN")
                .and()
                .withUser("user")
                .password("{sha256}53b3404d8ba159ba9a82582e09efb8071f5695b9c9dbf0697ed98e6add4eaece32f305f31525e0e3") //To setup password encoder use {}, noop is none
                .roles("USER")
                .and()
                .withUser("scott")
                .password("{bcrypt10}$2a$10$95.codS13rzEI7x71N6d2u2DBZTBQwSl4InB50mYjVuPfpVgYH5uK") //To setup password encoder use {}, noop is none
                .roles("CUSTOMER");
    }
    */

    /* @Override
    @Bean
    protected UserDetailsService userDetailsService() {
        UserDetails user =  User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();

        UserDetails admin =  User.withDefaultPasswordEncoder()
                .username("spring")
                .password("guru")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user, admin);
    }*/
}
