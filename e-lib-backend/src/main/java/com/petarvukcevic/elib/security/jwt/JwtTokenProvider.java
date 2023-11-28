package com.petarvukcevic.elib.security.jwt;

import com.petarvukcevic.elib.entities.User;
import com.petarvukcevic.elib.repositories.UserRepository;
import com.petarvukcevic.elib.security.jwt.dto.JwtTokenDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final String AUTH_KEY = "auth";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.validityInMinutes}")
    private long tokenValidityInMinutes;

    @Value("${jwt.refreshTokenValidityInMinutes}")
    private long refreshTokenValidityInMinutes;
    private final UserRepository userRepository;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public JwtTokenDTO generateToken(Authentication authentication, boolean rememberMe)
    {
        // Collection<GrantedAuthority>

        String authorities = authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(",")); // ADMINISTRATOR,MANAGER (role = String)

        // validity
        long now = new Date().getTime();
        Date accessTokenValidity = new Date(now + tokenValidityInMinutes * 60_000);

        String accessToken = Jwts.builder()
            .setSubject(authentication.getName()) // username
            .claim(AUTH_KEY, authorities)
            .signWith(SignatureAlgorithm.HS512, secretKey) // symmetric encryption...
            .setExpiration(accessTokenValidity) // validity of token (time in future)
            .compact();

        String refreshToken = null;
        if (rememberMe)
        {
            Date refreshTokenValidity = new Date(now + refreshTokenValidityInMinutes * 60_000);
            refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTH_KEY, authorities)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .setExpiration(refreshTokenValidity)
                .compact();
        }
        return new JwtTokenDTO(accessToken, refreshToken); // accessToken = required, refreshToken=optional
    }

    /**
     * Create authentication from token
     *
     * @param token jwt token
     * @return Authentication Object
     */
    public Authentication getAuthentication(String token)
    {
        Claims claims = Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody();

        String username = claims.getSubject();
        Collection<? extends GrantedAuthority> authorities = Arrays
            .stream(claims.get(AUTH_KEY).toString().split(",")) // [ROLE_ADMIN, ROLE_DEVELOPER]
            .map(SimpleGrantedAuthority::new)
            .toList();

        return new UsernamePasswordAuthenticationToken(username, "", authorities);
    }

    /**
     * Validate token
     *
     * @param authToken jwt token
     * @return boolean value (is valid | not)
     */
    public boolean validateToken(String authToken)
    {
        Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
        return true;
    }

    public JwtTokenDTO createTokenAfterVerifiedOtp(String username, boolean rememberMe)
    {
        User user = userRepository.findByUsernameWithRoles(username).orElseThrow(EntityNotFoundException::new);
        List<GrantedAuthority> authorities = user.getRoles().stream().map(role -> {
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role.getName());
            return grantedAuthority;
        }).toList();

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, user.getPassword(), authorities);
        return generateToken(authentication, rememberMe);
    }
}