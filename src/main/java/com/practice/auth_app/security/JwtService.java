package com.practice.auth_app.security;

import com.practice.auth_app.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//before completing this part must have to configure it in the applcation-dev.yaml file

@Service
@Getter
@Setter
public class JwtService {

    private  final SecretKey key;
    private final long accessTtlSeconds;
    private  final long refreshTtlSeconds;
    private final String issuer;

    public JwtService(
                     @Value("${security.jwt.secret}") String secrect,
                     @Value("${security.jwt.access-ttl-second}") long accessTtlSeconds,
                     @Value("${security.jwt.refresh-ttl-second}") long refreshTtlSeconds,
                     @Value("${security.jwt.issuer}") String issuer) {

        if (secrect==null|| secrect.length()<64){
            throw new IllegalArgumentException("Invalid secrets");
        }
        this.key = Keys.hmacShaKeyFor(secrect.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    //generate Accesstoken

    public String generateAccessToken(User user ){
        Instant now = Instant.now();
        List<String> roles = user.getRoles()==null ? List.of() :
                user.getRoles().stream().map((Roles)->Roles.getName()).toList();

        return Jwts.builder().
                id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(Map.of(
                        "email",user.getEmail(),
                        "roles",roles,
                        "typ","access"
                ))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

    }
    //generating refresh token

    public String generateRefreshToken(User user ,String jti ){
        Instant now = Instant.now();
        List<String> roles = user.getRoles()==null ? List.of() :
                user.getRoles().stream().map((Roles)->Roles.getName()).toList();

        return Jwts.builder().
                id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claim("typ","refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

    }

    //parse the token

    public Jws<Claims> parse(String token){
        //here all checks happend like validation , expiration
        try{
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        }catch (JwtException e){
          throw e;
        }
    }



    //checking the token is access token
    public boolean isAccessToken(String token){
        Claims c= parse(token).getPayload();
        return  "access".equals(c.get("typ"));
    }

    //checking the token is refresh or refresh token
    public boolean isRefreshToken(String token){
        Claims c= parse(token).getPayload();
        return  "refresh".equals(c.get("typ"));
    }

    //getting user id from token
    public UUID getUserId(String token){
        Claims c= parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }

    //getting jti id from the token
    public String getJti(String token){
        return parse(token).getPayload().getId();
    }

    public List<String> getEmail(String token){
        Claims c = parse(token).getPayload();
        return (List<String>) c.get("roles");
    }



}
