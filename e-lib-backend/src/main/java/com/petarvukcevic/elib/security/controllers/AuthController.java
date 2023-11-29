package com.petarvukcevic.elib.security.controllers;

import com.petarvukcevic.elib.security.dto.VerifyOtpDTO;
import com.petarvukcevic.elib.security.jwt.JwtTokenProvider;
import com.petarvukcevic.elib.security.jwt.dto.JwtTokenDTO;
import com.petarvukcevic.elib.security.jwt.dto.LoginDTO;
import com.petarvukcevic.elib.security.otp.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/authenticate")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final OtpService otpService;

    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestBody LoginDTO login)
    {
        try {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    login.getUsername(),
                    login.getPassword()
            );
            Authentication auth = authenticationManager.authenticate(authentication);
            log.info("Authentication after successful login: {}", auth);
            
            otpService.generateOtpAndSendEmail(login.getUsername());

//            JwtTokenDTO token = tokenProvider.generateToken(auth, login.isRememberMe());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204
        }
        catch (Exception e) {
            log.error("Error occured on login. Message {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("verify")
    public ResponseEntity<JwtTokenDTO> verify(@RequestBody VerifyOtpDTO verifyOtpDTO)
    {
        boolean isOtpValid = otpService.isOtpValid(verifyOtpDTO.getUsername(), verifyOtpDTO.getOtpCode());
        if (!isOtpValid) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        JwtTokenDTO tokenDTO = tokenProvider.createTokenAfterVerifiedOtp(
                verifyOtpDTO.getUsername(),
                verifyOtpDTO.isRememberMe()
        );

        return new ResponseEntity<>(tokenDTO, HttpStatus.CREATED);
    }

}
