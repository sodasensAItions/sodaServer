package com.sodasensaitions.backend.utils;

import com.sodasensaitions.backend.authentication.auth.pojo.AuthenticationResponse;
import com.sodasensaitions.backend.authentication.auth.pojo.RegisterRequest;
import com.sodasensaitions.backend.authentication.user.Account;
import com.sodasensaitions.backend.config.constants.HttpServletSessionConstants;
import lombok.AllArgsConstructor;
import org.junit.Assert;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;


@AllArgsConstructor
public class RegisteringUtils {

  private int port;
  private TestRestTemplate restTemplate;

  private String myURL() {
    return "http://localhost:" + port;
  }


  public ResponseEntity<AuthenticationResponse> tryRegistering(Account account, HttpStatus expectedStatus) {
    String url = myURL() + HttpServletSessionConstants.AUTHENTICATION_PATH + "/register";

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    RegisterRequest registerRequest = new RegisterRequest(account);
    HttpEntity<String> httpEntity = new HttpEntity<>(registerRequest.getAsJsonObject().toString(), httpHeaders);

    ResponseEntity<AuthenticationResponse> responseEntity = restTemplate.postForEntity(url, httpEntity, AuthenticationResponse.class);
    Assert.assertEquals(expectedStatus, responseEntity.getStatusCode());

    AuthenticationResponse body = responseEntity.getBody();
    if (HttpStatus.OK == expectedStatus) {
      Assert.assertNotNull(body);
      account.setId(body.getAccountID());
    }
    return responseEntity;
  }

  public void logout(AuthenticationResponse request) {
    String url = myURL() + HttpServletSessionConstants.AUTHENTICATION_PATH + "/logout";
    HttpHeaders httpHeaders = new HttpHeaders();
    String accessToken = request.getAccessToken();
    Assert.assertNotNull(accessToken);
    httpHeaders.setBearerAuth(accessToken);
    HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
    restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
  }

  public ResponseEntity<AuthenticationResponse> login(Account account, HttpStatus expectedStatus) {
    String url = myURL() + HttpServletSessionConstants.AUTHENTICATION_PATH + "/authenticate";

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    RegisterRequest registerRequest = new RegisterRequest(account);
    HttpEntity<String> httpEntity = new HttpEntity<>(registerRequest.getAsJsonObject().toString(), httpHeaders);

    ResponseEntity<AuthenticationResponse> responseEntity = restTemplate.postForEntity(url, httpEntity, AuthenticationResponse.class);
    Assert.assertEquals(expectedStatus, responseEntity.getStatusCode());

    AuthenticationResponse body = responseEntity.getBody();
    if (HttpStatus.OK == expectedStatus) {
      Assert.assertNotNull(body);
      account.setId(body.getAccountID());
    }

    return responseEntity;
  }

  public String testReachSecuredEndpoint(AuthenticationResponse authenticationResponse, HttpStatus expectedStatus) {
    Assert.assertNotNull(authenticationResponse);
    String token = authenticationResponse.getAccessToken();
    Assert.assertNotNull(token);

    String url = myURL() + "/test/unreachable";
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    HttpEntity<String> httpEntity = new HttpEntity<>(headers);

    ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    Assert.assertEquals(expectedStatus, response.getStatusCode());
    System.out.println(response.getBody() + " has expected status " + expectedStatus);
    return response.getBody();
  }

}
