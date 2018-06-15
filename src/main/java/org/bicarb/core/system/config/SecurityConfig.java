/*
 * Copyright (c) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bicarb.core.system.config;

import java.util.concurrent.TimeUnit;
import org.bicarb.core.system.security.BicarbAuthenticationFailureHandler;
import org.bicarb.core.system.security.BicarbAuthenticationSuccessHandler;
import org.bicarb.core.system.security.BicarbPersistentTokenRepository;
import org.bicarb.core.system.security.BicarbSessionRegistry;
import org.bicarb.core.system.security.BicarbUserDetailsService;
import org.bicarb.core.system.security.Http401UnauthenticatedEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security Config.
 *
 * @author olOwOlo
 */
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final BicarbUserDetailsService userDetailsService;
  private final BicarbPersistentTokenRepository persistentTokenRepository;
  private final BicarbAuthenticationSuccessHandler bicarbAuthenticationSuccessHandler;
  private final BicarbAuthenticationFailureHandler bicarbAuthenticationFailureHandler;

  /**
   * Constructor.
   */
  @Autowired
  public SecurityConfig(
      BicarbUserDetailsService userDetailsService,
      BicarbPersistentTokenRepository persistentTokenRepository,
      BicarbAuthenticationSuccessHandler bicarbAuthenticationSuccessHandler,
      BicarbAuthenticationFailureHandler bicarbAuthenticationFailureHandler) {
    this.userDetailsService = userDetailsService;
    this.persistentTokenRepository = persistentTokenRepository;
    this.bicarbAuthenticationSuccessHandler = bicarbAuthenticationSuccessHandler;
    this.bicarbAuthenticationFailureHandler = bicarbAuthenticationFailureHandler;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .headers()
            .httpStrictTransportSecurity().disable()
        .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/admin/**").hasAuthority("admin")
            .antMatchers(HttpMethod.POST, "/api/admin").permitAll()
            .antMatchers(HttpMethod.POST, "/api/user").permitAll()
            .antMatchers(HttpMethod.POST, "/api/user/password/reset").permitAll()
            .antMatchers(HttpMethod.POST, "/api/user/password/reset/send").permitAll()
            .antMatchers(HttpMethod.POST).authenticated()
            .antMatchers(HttpMethod.PATCH).authenticated()
            .antMatchers(HttpMethod.DELETE).authenticated()
            .antMatchers(HttpMethod.GET).permitAll()
            .anyRequest().denyAll()
        .and()
            .exceptionHandling().authenticationEntryPoint(new Http401UnauthenticatedEntryPoint())
        .and()
            .rememberMe()
            .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(60))
            .tokenRepository(persistentTokenRepository)
            .userDetailsService(userDetailsService)
        .and()
            .sessionManagement().maximumSessions(-1).sessionRegistry(sessionRegistry()).and()
        .and()
            .formLogin()
            .loginPage("/login")  // explicit set avoid generate default login page
            .successHandler(bicarbAuthenticationSuccessHandler)
            .failureHandler(bicarbAuthenticationFailureHandler)
        .and()
            .logout()
            .logoutSuccessUrl("/");
  }

  /**
   * DelegatingPasswordEncoder.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  /**
   * DaoAuthenticationProvider.
   */
  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setPasswordEncoder(passwordEncoder());
    authProvider.setUserDetailsService(userDetailsService);
    return authProvider;
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(daoAuthenticationProvider());
  }

  @Bean
  public BicarbSessionRegistry sessionRegistry() {
    return new BicarbSessionRegistry();
  }
}
