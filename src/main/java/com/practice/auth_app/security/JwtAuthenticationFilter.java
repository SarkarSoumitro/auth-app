package com.practice.auth_app.security;
import com.practice.auth_app.repositories.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final  JwtService jwtService;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);




    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       String header = request.getHeader("Authorization");

       logger.info("Authorization header : {}",header);

       if (header!=null && header.startsWith("Bearer ")){
           //token extract
           //then it validate
           //authentication creat
           //store it in the security context
           String token = header.substring(7);

           try{
               if (!jwtService.isAccessToken(token)){
                   filterChain.doFilter(request,response);
                   return;
               }

               Jws<Claims> parse = jwtService.parse(token);
               Claims payload = parse.getPayload();
               String userId = payload.getSubject();
               UUID userUUid = UUID.fromString(userId);

               userRepository.findById(userUUid).ifPresent(
                      user -> {
                          //checking is user is enabled or not means account is disabled or not
                          if (user.isEnable()){
                              List<GrantedAuthority> authorities = user.getRoles()==null ? List.of():
                                      user.getRoles().stream().map(roles ->
                                                                   new SimpleGrantedAuthority(roles.getName())
                                      ).collect(Collectors.toList());
                              UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                      user.getEmail(),
                                      null,
                                      authorities
                              );
                              authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                              //final line which will store  or remember the info from token that is the valid request to access any protected apis
                              if (SecurityContextHolder.getContext().getAuthentication()==null){
                                  SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                              }
                          }


                      }
               );

           }catch (ExpiredJwtException e){
                e.printStackTrace();
           }catch (MalformedJwtException e){
               e.printStackTrace();

           }catch (JwtException e){
               e.printStackTrace();

           }catch (Exception e){
               e.printStackTrace();

           }

       }
       filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/auth/login");
    }
}
