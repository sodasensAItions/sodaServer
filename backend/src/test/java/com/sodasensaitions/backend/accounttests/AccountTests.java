package com.sodasensaitions.backend.accounttests;

import com.sodasensaitions.backend.authentication.auth.pojo.AuthenticationResponse;
import com.sodasensaitions.backend.authentication.user.Account;
import com.sodasensaitions.backend.authentication.user.AccountRepository;
import com.sodasensaitions.backend.utils.RegisteringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountTests {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private RegisteringUtils registeringUtils;

  @Autowired
  private AccountRepository accountRepository;

  @Lazy
  @TestConfiguration
  static class CustomerAccountsTestConfiguration {
    @Bean
    public RegisteringUtils registeringUtils(@Value("${local.server.port}") int port, @Autowired TestRestTemplate restTemplate) {
      return new RegisteringUtils(port, restTemplate);
    }
  }

  private String myURL() {
    return "http://localhost:" + port;
  }

  //TODO Add account tests here

  @Test
  public void testRegisterAccount() {
    Account account = new Account("testRegisterAccount", "testRegisterAccont@usu.edu", "mySecretPassword", "John", "Doe");
    registeringUtils.tryRegistering(account, HttpStatus.OK);

    //verify that the account was created in the database
    accountRepository.findByUsername(account.getUsername()).ifPresentOrElse(
      user -> {
        assert user.getUsername().equals(account.getUsername());
        assert user.getEmail().equals(account.getEmail());
        assert user.getFirstName().equals(account.getFirstName());
        assert user.getLastName().equals(account.getLastName());
        System.out.println("The password is: " + user.getPassword()); //visually verify that the password is hashed
      },
      () -> {
        assert false;
      }
    );
  }

  @Test
  public void testRegisterAccountWithExistingUsername() {
    Account account = new Account("testRegisterAccountWithExistingUsername",
        "testRegisterAccountWithExistingUsername@usu.edu",
        "mySecretPassword", "John", "Doe");
    registeringUtils.tryRegistering(account, HttpStatus.OK);
    registeringUtils.tryRegistering(account, HttpStatus.CONFLICT);

    //verify that the account was created in the database only once
    long count = accountRepository.findByUsername(account.getUsername()).stream().count();
    assert count == 1;
  }

  @Test
  public void testAccessingProtectedEndpoint() {
    ResponseEntity<String> responseEntity = restTemplate.getForEntity(myURL() + "/test/unreachable", String.class);
    Assert.assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    System.out.println(responseEntity.getBody() + " has expected status " + HttpStatus.FORBIDDEN);
  }

  @Test
 public void testLogout(){
    Account account = new Account("testLogout", "testLogout@usu.edu", "mySecretPassword", "John", "Doe");
    ResponseEntity<AuthenticationResponse> responseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    //verify that we can reach private endpoint
    AuthenticationResponse authenticationResponse = responseEntity.getBody();
    registeringUtils.testReachSecuredEndpoint(authenticationResponse, HttpStatus.OK);

    //verify that we can logout
    assert authenticationResponse != null;
    registeringUtils.logout(authenticationResponse);

    //verify that we can no longer reach private endpoint
    registeringUtils.testReachSecuredEndpoint(authenticationResponse, HttpStatus.FORBIDDEN);

    //verify that we can still reach public endpoint
    ResponseEntity<String> responseEntity2 = restTemplate.getForEntity(myURL() + "/test/public", String.class);
    Assert.assertEquals(HttpStatus.OK, responseEntity2.getStatusCode());
  }

  @Test
  public void testLogin(){
    Account account = new Account("testLogin", "testLogin@usu.edu", "mySecretPassword", "John", "Doe");
    registeringUtils.tryRegistering(account, HttpStatus.OK);

    //verify that we can reach private endpoint
    ResponseEntity<AuthenticationResponse> responseEntity = registeringUtils.login(account, HttpStatus.OK);
    AuthenticationResponse authenticationResponse = responseEntity.getBody();
    registeringUtils.testReachSecuredEndpoint(authenticationResponse, HttpStatus.OK);

    //logout and login 10 times
    for(int i = 0; i < 10; i++){
      assert authenticationResponse != null;
      registeringUtils.logout(authenticationResponse);
      registeringUtils.testReachSecuredEndpoint(authenticationResponse, HttpStatus.FORBIDDEN);
      responseEntity = registeringUtils.login(account, HttpStatus.OK);
      authenticationResponse = responseEntity.getBody();
      registeringUtils.testReachSecuredEndpoint(authenticationResponse, HttpStatus.OK);
    }
  }
}
