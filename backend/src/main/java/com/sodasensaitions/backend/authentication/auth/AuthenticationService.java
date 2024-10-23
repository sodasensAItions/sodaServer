package com.sodasensaitions.backend.authentication.auth;

import com.sodasensaitions.backend.authentication.auth.pojo.AuthenticationRequest;
import com.sodasensaitions.backend.authentication.auth.pojo.AuthenticationResponse;
import com.sodasensaitions.backend.authentication.auth.pojo.RegisterRequest;
import com.sodasensaitions.backend.authentication.token.JwtService;
import com.sodasensaitions.backend.authentication.token.Token;
import com.sodasensaitions.backend.authentication.token.TokenRepository;
import com.sodasensaitions.backend.authentication.user.Account;
import com.sodasensaitions.backend.authentication.user.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final AccountRepository accountRepository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthenticationResponse register(RegisterRequest request) {

    Optional<Account> optional = accountRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail());
    if (optional.isPresent()) {
      return null;
    }

    var user = Account.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .username(request.getUsername())
        .firstName(request.getFirstname())
        .lastName(request.getLastname())
        .build();
    var savedUser = accountRepository.save(user);
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser, jwtToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
        .accountID(savedUser.getId())
        .build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword()
        )
    );
    var user = accountRepository.findByUsername(request.getUsername())
        .orElse(null);
    if (user == null) {
      return null;
    }

    List<Token> tokens = user.getTokens();
    for (Token token : tokens) {
      if (!token.expired) {
        var refreshToken = jwtService.generateRefreshToken(user);
        return AuthenticationResponse.builder().accessToken(token.getToken()).refreshToken(refreshToken).accountID(user.getId()).build();
      }
    }

    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);

    saveUserToken(user, jwtToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
        .build();
  }

  public void saveUserToken(Account account, String jwtToken) {
    var token = Token.builder()
        .account(account)
        .token(jwtToken)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepository.save(token);
  }

}
