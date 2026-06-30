package com.practice.auth_app.controllers;

import com.practice.auth_app.dtos.LoginRequest;
import com.practice.auth_app.dtos.RefreshTokenRequest;
import com.practice.auth_app.dtos.TokenResponse;
import com.practice.auth_app.dtos.UserDto;
import com.practice.auth_app.entities.RefreshToken;
import com.practice.auth_app.entities.User;
import com.practice.auth_app.repositories.RefreshTokenRepository;
import com.practice.auth_app.repositories.UserRepository;
import com.practice.auth_app.security.CookieService;
import com.practice.auth_app.security.JwtService;
import com.practice.auth_app.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.catalina.users.SparseUserDatabase;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private  final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private  final ModelMapper mapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;


    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
            ){

        //authenticate
        authenticate(loginRequest);
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(()->new BadCredentialsException("Invalid Email or password"));
        if (!user.isEnable()){
            throw new DisabledException("User is disabled");
        }

        //generating refresh token (which will be store in the database(i means it's id will be saved in the database to be tracked) and later it can be replaced  by the revoked one)
        String jti = UUID.randomUUID().toString();
        var refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();


        //now saving refresh token id and other info in the database
        refreshTokenRepository.save(refreshTokenOb);



        //generate  access token
        String accessToken = jwtService.generateAccessToken(user);
        //generating refresh token
        String refreshToken = jwtService.generateRefreshToken(user,refreshTokenOb.getJti());


        //use cookie service to add refresh to the cookie
        cookieService.addRefreshTokenCookie(refreshToken, jwtService.getRefreshTtlSeconds(), response);
        cookieService.addNoStoreHeader(response);


        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTtlSeconds(), mapper.map(user, UserDto.class));
        return ResponseEntity.ok(tokenResponse);

    }




    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));

    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        String refreshToken = readRefreshTokenFromRequest(body, request).orElseThrow(() -> new BadCredentialsException("Refresh token is missing"));
        
        jwtService.parse(refreshToken);
        String jti = jwtService.getJti(refreshToken);
        
        RefreshToken storedToken = refreshTokenRepository.findByJti(jti).orElseThrow(() -> new BadCredentialsException("Refresh token not found"));
        
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        
        cookieService.removeRefreshTokenCookie(response);
        cookieService.addNoStoreHeader(response);
        
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Logged out successfully",
                "timestamp", Instant.now()
        ));
    }

    // this api for renew access and refresh token
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
          @RequestBody(required = false)  RefreshTokenRequest body,
          HttpServletRequest request,
          HttpServletResponse response
    ){

        String refreshToken = readRefreshTokenFromRequest(body,request).orElseThrow(()->new BadCredentialsException("Refresh token is mission !!"));

        if (!jwtService.isRefreshToken(refreshToken)){
            throw  new BadCredentialsException("Invalid refresh token type");
        }
        String jti = jwtService.getJti(refreshToken);
        UUID userId = jwtService.getUserId(refreshToken);
        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti).orElseThrow(() -> new BadCredentialsException("Refresh token not recognized."));

      //checking validation

       if (storedRefreshToken.isRevoked()){
           throw  new BadCredentialsException("Refresh token is revoked.");
       }
       if (storedRefreshToken.getExpiredAt().isBefore(Instant.now())){
           throw  new BadCredentialsException("Refresh token is expired.");
       }
       if (!storedRefreshToken.getUser().getId().equals(userId)){
           throw new BadCredentialsException("Refresh token does not belong to this user");
       }

       //rotating refresh token
        //here just we change the refresh token id from database from old one to new generated one
        storedRefreshToken.setRevoked(true);
        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        //now we will create a new refresh token info corresponding the newjti store in database
        User user = storedRefreshToken.getUser();

        var newRefreshTokenob = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshTokenob);
//here in this part we are generating the new acceccss token and refresh token because after a cetain cycle of time they got expired and now we create one and the above work all about database storation and info creation of work .
     String newAccessToken = jwtService.generateAccessToken(user);
     String newRefreshToken = jwtService.generateRefreshToken(user, newRefreshTokenob.getJti());

     cookieService.addRefreshTokenCookie(newRefreshToken, jwtService.getRefreshTtlSeconds(), response);
     cookieService.addNoStoreHeader(response);
     return ResponseEntity.ok(TokenResponse.of(newAccessToken,newRefreshToken, jwtService.getRefreshTtlSeconds(), mapper.map(user,UserDto.class)));
    }

    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
         //1. prefer reading refresh token from cookie
        if (request.getCookies()!=null){
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> !v.isBlank())
                    .findFirst();
            if (fromCookie.isPresent()){
                return fromCookie;
            }
        }

        //2. from body
        if (body!=null && body.refreshToken()!=null && !body.refreshToken().isBlank()){
          return   Optional.of(body.refreshToken());
        }

        //3. from header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader!=null && !refreshHeader.isBlank()){
            return Optional.of(refreshHeader.trim());
        }

        //4. Authorization Bearer token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7).trim());
        }

        return Optional.empty();
    }


    //authenticate part
    private void authenticate (LoginRequest loginRequest){
      try{
          authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

      }catch (Exception e){
          throw  new BadCredentialsException("Password or email is not valid!!!!");
      }

    }
}
