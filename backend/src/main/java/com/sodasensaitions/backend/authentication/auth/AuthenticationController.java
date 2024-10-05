package com.sodasensaitions.backend.authentication.auth;

import com.sodasensaitions.backend.authentication.auth.pojo.AuthenticationRequest;
import com.sodasensaitions.backend.authentication.auth.pojo.AuthenticationResponse;
import com.sodasensaitions.backend.authentication.auth.pojo.RegisterRequest;
import com.sodasensaitions.backend.config.constants.HttpServletSessionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(HttpServletSessionConstants.AUTHENTICATION_PATH)
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody RegisterRequest request
  ) {
    AuthenticationResponse authenticationResponse = authenticationService.register(request);
    if (authenticationResponse == null) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
    return ResponseEntity.ok(authenticationResponse);
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody AuthenticationRequest request
  ) {
    AuthenticationResponse authenticationResponse = authenticationService.authenticate(request);
    if (authenticationResponse == null) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    return ResponseEntity.ok(authenticationResponse);
  }

}
