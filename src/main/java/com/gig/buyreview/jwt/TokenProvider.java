package com.gig.buyreview.jwt;

import com.gig.buyreview.domain.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.nio.charset.MalformedInputException;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @author : Jake
 * @date : 2022-01-08
 */
@Slf4j
@Component
public class TokenProvider implements InitializingBean {

    private static final String AUTHORITIES_KEY = "auth";

    private final String secret;
    private final long tokenValidityInMillisecond;

    private Key key;

    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInMillisecond) {
        this.secret = secret;
        this.tokenValidityInMillisecond = tokenValidityInMillisecond;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // application.yml 에서 설정한 만료시간
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMillisecond);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }


    /**
     * Token 에 담겨있는 정보를 이용해 Authentication 객체를 리턴하는 메소드
     */
    public Authentication getAuthentication(String token) {
        // token 을 이용해 Claims 을 만들어준다.
        Claims claims = Jwts.
                parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Claims 에서 권한정보를 가져온다.
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 권한정보를 이용해 User 객체를 만든다.
        User principal = new User(claims.getSubject(), "", authorities);

        // 권한정보, User, token 을 이용해 Authentication 을 리턴한다.
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }

        return false;
    }
}

/**
 * InitialBean 을 통해
 * Bean 이 생성 되고 주입된 Secret 값을 Base64로 Decode 해서 key 에 Set 하기 위함
 *
 * authentication 객체의 권한정보를 이용해서 토큰을 생성하는 createToken 메소드
 */
