package com.gig.buyreview.config;

import com.gig.buyreview.jwt.JwtAccessDeniedHandler;
import com.gig.buyreview.jwt.JwtAuthenticationEntryPoint;
import com.gig.buyreview.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author : Jake
 * @date : 2022-01-08
 */
@EnableWebSecurity
// @PreAuthorize 어노테이션을 메소드 단위로 사용할 수 있게 해줌.
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers(
                        "/h2-console/**",
                        "/favicon.ico"
                );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 우리는 토큰 방식을 사용하기 때문에 csrf Disabled 처리
                .csrf().disable()

                // Exception Handling 할 때 커스텀 Exception 설정
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                // H2 콘솔을 위한 설정
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()

                // 세션을 사용하지 않기 때문에 세션을 STATELESS 로 설정
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // 로그인과 회원가입 시에는 토큰이 없음으로 예외처리를 해줌
                .and()
                .authorizeRequests()
                .antMatchers("/api/hello").permitAll()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/signup").permitAll()
                .anyRequest().authenticated()

                // jwt 필터를 시큐리티 설정에 적용
                // addFilterBefore 로 설정해두었던 CONFIG 파일
                .and()
                .apply(new JwtSecurityConfig(tokenProvider));
    }

}

/**
 * @EnableWebSecurity : 기본적인 Web 보안 활성화
 * WebSecurityConfigure 을 implements 하거나
 * WebSecurityConfigurerAdapter 를 extends 하는 방법
 *
 * authorizeRequests : 권한관련 요청에 대한 설정을 하겠다.
 * permitAll : 인증 없이 접근 허용
 * anyRequest().authenticated() 이외의 모든 요청 인증 체크
 */
