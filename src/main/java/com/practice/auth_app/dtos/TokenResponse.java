package com.practice.auth_app.dtos;


public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expireIn,
        String tokenType,
        UserDto userdto
) {
   public static TokenResponse of(String accessToken, String refreshToken, long expireIn, UserDto userDto){
       return  new TokenResponse(accessToken, refreshToken, expireIn, "Bearer", userDto);
   }
}
